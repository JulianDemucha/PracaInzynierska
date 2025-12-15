package com.soundspace.service.song;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.RecommendationsSongProjection;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsService {
    private final SongRepository songRepo;
    private final AppUserRepository appUserRepository;

    // zeby zmienic to 200 to juz lepiej - todo zrobic cachowanie
    // zeby przy kazdym odswiezeniu mainpage nie lecial request na milion songow (cachowanie i tak sie przyda nawet przy mniejszej ilosci)
    private static final int CANDIDATE_POOL_SIZE = 200;
    private static final int MINIMUM_CANDIDATES = 10;

    // wagi po ktorych przeliczane jest 'znaczenie' danych gatunkow i autorow
    // default 1.0 dla like
    // default 1.0 ustawia wage dislike tak, że dislike kasuje like (1:1)
    // default 1.5 robi ze favourite jest troche silniejszy niz like
    private static final double W_LIKE = 1.0;
    private static final double W_DISLIKE = 1.0;
    private static final double W_FAVOURITE = 1.5;

    // wagi bazowe, przez ktore mnozone sa wagi wyliczone po like, dislike, favourite
    private static final double W_GENRE = 0.5;
    private static final double W_AUTHOR = 0.3;
    private static final double W_VIEWS = 0.2;

    /*
        view cap liczony po 90 percentylu zeby same most-viewed piosenki nie zapychaly rekomendacji, tylko po
        pewnym (wyliczonym) poziomie wyswietlen, wieksza ilosc nie robi juz roznicy
     */
    private volatile double cachedViewCap;
    private volatile double cachedLogCap;


    @Transactional(readOnly = true)
    public Page<SongDto> getRecommendations(UserDetails userDetails, Pageable pageable) {
        if(userDetails == null) {
            return getGlobalTopSongs(pageable);
        }

        Long userId = appUserRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
        List<RecommendationsSongProjection> likedSongs = songRepo.findAllLikedByAppUserIdForRecommendations(userId);
        List<RecommendationsSongProjection> dislikedSongs = songRepo.findAllDislikedByAppUserIdForRecommendations(userId);
        List<RecommendationsSongProjection> favouriteSongs = songRepo.findAllFavouriteByAppUserIdForRecommendations(userId);

        // COLD START - jak user nic nie polubil ani nie-polubil to po prostu te z najwieksza iloscia wyswietlen
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty() && favouriteSongs.isEmpty()) {
            return getGlobalTopSongs(pageable);
        }

        Map<Genre, Double> genreWeights = calculateGenreProfile(likedSongs, dislikedSongs, favouriteSongs);
        Map<Long, Double> authorWeights = calculateAuthorProfile(likedSongs, dislikedSongs, favouriteSongs);

        // przekazywane genre to wszystkie ktore sa przynajmniej w jednej piosence ktora polubil
        List<Song> candidates = songRepo.findCandidates(
                genreWeights.keySet(),
                authorWeights.keySet(),
                userId,
                PageRequest.of(0, CANDIDATE_POOL_SIZE)
        );

        // BACKFILL - jezeli mniej niz 10 w candidates to uzupelniamy do tych min. 10 [MINIMUM_CANDIDATES] uzywajac popularnych
        if (candidates.size() < MINIMUM_CANDIDATES) {
            // do wypelnienia
            List<Song> fillers = songRepo.findTopPopularSongs(PageRequest.of(0, 50));

            // do wyfiltrowania znanych (juz zareagowanych) piosenek z fillers
            Set<Long> dislikedIds = dislikedSongs.stream().map(RecommendationsSongProjection::getId).collect(Collectors.toSet());
            Set<Long> likedIds = likedSongs.stream().map(RecommendationsSongProjection::getId).collect(Collectors.toSet());
            Set<Long> favIds = favouriteSongs.stream().map(RecommendationsSongProjection::getId).collect(Collectors.toSet());

            for (Song s : fillers) {
                if (candidates.size() >= MINIMUM_CANDIDATES) break;

                // %Ids zamiast %Songs.contains(s), bo pozniej likedSongs, dislikedSongs, favouriteSongs zwraca projekcje
                // fillers nie posiada takich samych projekcji, tylko cale songi (cos niecos trzeba zwrocic) wiec porownanie calych obiektow nie zadziala
                // = po id bedzie g
                if (!candidates.contains(s)
                        && !dislikedIds.contains(s.getId())
                        && !likedIds.contains(s.getId())
                        && !favIds.contains(s.getId())) {
                    candidates.add(s);
                }
            }
        }


        return toPage(

                // docelowy scoring i sortowanie
                candidates.stream()
                .map(song -> {
                    double score = calculateScore(song, genreWeights, authorWeights, cachedLogCap);
                    return new ScoredSong(song, score);
                })
                .sorted(Comparator.comparingDouble(ScoredSong::score).reversed())
                .map(scoredSong -> SongDto.toDto(scoredSong.song()))
                .toList(),

                pageable
        );
    }

    /// helpery

    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void updateViewCap() {
        long newCap = songRepo.findViewCountPercentile90().orElse(0L);

        if (newCap < 1) newCap = 1;

        this.cachedViewCap = (double) newCap;
        this.cachedLogCap = Math.log10(1 + this.cachedViewCap);

        log.info("Zaktualizowano ViewCap (Cache): {}", newCap);
    }

    /// Pojedyńczy song trafia do metody, a następnie:
    /// * wyliczana jest średnia waga (ze wszystkich gatunków piosenki).
    /// * wyliczana jest waga dla autora.
    /// * wyliczana jest 'waga wyświetleń' -
    /// czyli jak duże znaczenie pod względem wyświetleń ma piosenka względem reszty (do pewnego momentu - {@link #cachedViewCap}  - 90 percentyl)
    private double calculateScore(Song song, Map<Genre, Double> genreProfile, Map<Long, Double> authorProfile, double logCap) {
        // pobiera gatunki songa i dla kazdego z nich: (jezeli istnieje w genreProfile zlicza jego wage)
        // nastepnie zapisuje do genreScore ŚREDNIĄ Z TYCH WAG (zapobiega exploitowaniu i braniu 3 gatunkow dla samych korzysci)
        double genreScore = song.getGenres().stream()
                .mapToDouble(g -> genreProfile.getOrDefault(g, 0.0))
                .average()
                .orElse(0.0);

        double authorScore = authorProfile.getOrDefault(song.getAuthor().getId(), 0.0);

        double rawViewScore = Math.log10(1 + song.getViewCount()) / logCap;
        double viewScore = Math.min(rawViewScore, 1.0); // 1.0 to uciecie 10% wyzszych (90 percentyl)

        // docelowe wyliczenie średniej ważonej / score
        return (genreScore * W_GENRE) +
                (authorScore * W_AUTHOR) +
                (viewScore * W_VIEWS);
    }

    private Page<SongDto> getGlobalTopSongs(Pageable pageable) {
        return songRepo.findTopPopularSongsPage(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(SongDto::toDto);
    }

    // todo wrzucic to arcydzielo gdzies indziej bo przyda sie przy przerabianiu innych metod do wysylania page zamiast listy
    // zamienia liste songdto na Page, wycinając odpowiedni fragment
    private static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        Objects.requireNonNull(list, "obiekt list nie moze byc pusty");
        Objects.requireNonNull(pageable, "obiekt pageable nie moze byc pusty");

        int total = list.size();
        int start = (int) pageable.getOffset();

        if (start >= total) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        int end = Math.min(start + pageable.getPageSize(), total);

        // subList zwraca widok - opakowanie w arraylist zeby skopiowac
        List<T> pageContent = new ArrayList<>(list.subList(start, end));

        return new PageImpl<>(pageContent, pageable, total);
    }


    /// Tworzy mapę (Gatunek -> Waga Gatunku),
    /// gdzie waga każdego gatunku wyliczana jest po tym jak duży udział dany gatunek ma w polubionych/nie-polubionych/ulubionych piosenkach użytkownika
    private Map<Genre, Double> calculateGenreProfile(List<RecommendationsSongProjection> likedSongs,
                                                     List<RecommendationsSongProjection> dislikedSongs,
                                                     List<RecommendationsSongProjection> favouriteSongs) {
        // na wszelki mimo ze w getRecoomendations wtedy i tak pojdzie topsongs
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty() && favouriteSongs.isEmpty()) return Collections.emptyMap();

        Map<Genre, Double> scores = new HashMap<>();

        // zeby pominac favourite przy iteracji po likedSongs
        Set<Long> favIds = favouriteSongs.stream().map(RecommendationsSongProjection::getId).collect(Collectors.toSet());

        // do normalizacji
        double currentTotalMass = 0.0;

        // liczy wagi dla konkretnych gatunkow po favourite
        for (RecommendationsSongProjection song : favouriteSongs) {
            double contribution = (1.0 / song.getGenres().size()) * W_FAVOURITE;
            for (Genre g : song.getGenres()) {
                scores.merge(g, contribution, Double::sum);
            }
            currentTotalMass += W_FAVOURITE;
        }

        // liczy wagi dla konkretnych gatunkow po like, pomijajac songi ktore juz sa favourite
        for (RecommendationsSongProjection song : likedSongs) {
            if (favIds.contains(song.getId())) continue; // jak song jest tez favourite to nara

            double contribution = (1.0 / song.getGenres().size()) * W_LIKE;
            for (Genre g : song.getGenres()) {
                scores.merge(g, contribution, Double::sum);
            }
            currentTotalMass += W_LIKE;
        }

        // liczy UJEMNE wagi dla konkretnych gatunkow po dislike
        for (RecommendationsSongProjection song : dislikedSongs) {
            // Waga ujemna
            double contribution = (1.0 / song.getGenres().size()) * W_DISLIKE;
            for (Genre g : song.getGenres()) {
                scores.merge(g, -contribution, Double::sum);
            }
            currentTotalMass += W_DISLIKE;
        }



        if (currentTotalMass == 0) return Collections.emptyMap();
        final double finalMass = currentTotalMass; // do mapowania przejdzie tylko final

        // liczy wagi ujemne dla konkretnych gatunkow po dislike + normalizacja dzielac przez total mass
        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / finalMass
                ));
    }

    /// Tworzy mapę (Autor -> Waga Autora),
    /// gdzie waga każdego autora wyliczana jest po tym jak duży udział dany autor ma w polubionych/nie-polubionych/ulubionych piosenkach użytkownika.
    private Map<Long, Double> calculateAuthorProfile(List<RecommendationsSongProjection> likedSongs,
                                                     List<RecommendationsSongProjection> dislikedSongs,
                                                     List<RecommendationsSongProjection> favouriteSongs) {
        // na wszelki mimo ze w getRecoomendations wtedy i tak pojdzie topsongs
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty() && favouriteSongs.isEmpty()) return Collections.emptyMap();

        Map<Long, Double> scores = new HashMap<>();

        // zeby pominac favourite przy iteracji po likedSongs
        Set<Long> favIds = favouriteSongs.stream().map(RecommendationsSongProjection::getId).collect(Collectors.toSet());

        // do normalizacji
        double currentTotalMass = 0.0;

        // liczy wagi dla konkretnych autorow po favourite
        for (RecommendationsSongProjection song : favouriteSongs) {
            scores.merge(song.getAuthorId(), W_FAVOURITE, Double::sum);
            currentTotalMass += W_FAVOURITE;
        }

        // liczy wagi dla konkretnych autorow po like, pomijajac songi ktore juz sa favourite
        for (RecommendationsSongProjection song : likedSongs) {
            if (favIds.contains(song.getId())) continue;

            scores.merge(song.getAuthorId(), W_LIKE, Double::sum);
            currentTotalMass += W_LIKE;
        }

        for (RecommendationsSongProjection song : dislikedSongs) {
            scores.merge(song.getAuthorId(), -W_DISLIKE, Double::sum);
            currentTotalMass += W_DISLIKE;
        }

        if (currentTotalMass == 0) return Collections.emptyMap();
        final double finalMass = currentTotalMass; // do mapowania przejdzie tylko final

        // liczy wagi ujemne dla konkretnych autorow po dislike + normalizacja dzielac przez total mass
        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / finalMass
                ));
    }


    // w sumie to mapa <Song, Double>
    private record ScoredSong(Song song, double score) {}
}