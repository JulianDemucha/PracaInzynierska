package com.soundspace.service;

import com.soundspace.dto.SongDto;
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
public class RecommendationService {

    private final ViewCapService statsService;
    private final SongRepository songRepo;
    private final AppUserRepository appUserRepository;

    // zeby zmienic to 200 to juz lepiej - todo zrobic cachowanie
    // zeby przy kazdym odswiezeniu mainpage nie lecial request na milion songow (cachowanie i tak sie przyda nawet przy mniejszej ilosci)
    private static final int CANDIDATE_POOL_SIZE = 200;
    private static final int MINIMUM_CANDIDATES = 10;

    // 1.0 ustawia wage dislike tak, że dislike kasuje like (1:1)
    private static final double W_DISLIKE = 1.0;

    private static final double W_GENRE = 0.5;
    private static final double W_AUTHOR = 0.3;
    private static final double W_VIEWS = 0.2;

    private volatile double cachedViewCap;
    private volatile double cachedLogCap;

    @Transactional(readOnly = true)
    public Page<SongDto> getRecommendations(UserDetails userDetails, Pageable pageable) {
        if(userDetails == null) {
            return getGlobalTopSongs(pageable);
        }

        Long userId = appUserRepository.findByEmail(userDetails.getUsername()).orElseThrow().getId();
        List<Song> likedSongs = songRepo.findAllLikedByAppUserId(userId);
        List<Song> dislikedSongs = songRepo.findAllDislikedByAppUserId(userId);

        // COLD START - jak user nic nie polubil ani nie-polubil to po prostu te z najwieksza iloscia wyswietlen
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty()) {
            return getGlobalTopSongs(pageable);
        }

        Map<Genre, Double> genreWeights = calculateGenreProfile(likedSongs, dislikedSongs);
        Map<Long, Double> authorWeights = calculateAuthorProfile(likedSongs, dislikedSongs);

        // przekazywane genre to wszystkie ktore sa przynajmniej w jednej piosence ktora polubil
        List<Song> candidates = songRepo.findCandidates(
                genreWeights.keySet(),
                authorWeights.keySet(),
                userId,
                PageRequest.of(0, CANDIDATE_POOL_SIZE)
        );

        // BACKFILL - jezeli mniej niz 10 w candidates to uzupelniamy do tych min. 10 [MINIMUM_CANDIDATES] uzywajac popularnych
        if (candidates.size() < MINIMUM_CANDIDATES) {
            List<Song> fillers = songRepo.findTopPopularSongs(PageRequest.of(0, 50));
            Set<Long> dislikedIds = dislikedSongs.stream()
                    .map(Song::getId)
                    .collect(Collectors.toSet());

            for (Song s : fillers) {
                if (candidates.size() >= MINIMUM_CANDIDATES) break;
                // todo edit komentarz jak sigma jak findallliked/disliked beda zwracac projection na to ze nie beda tylko juz zwracaja
                // dislikedIds zamiast dislikedSongs.contains(s), bo pozniej likedSongs i dislikedSongs beda zwracac projekcje
                if (!candidates.contains(s) && !dislikedIds.contains(s.getId())) {
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


    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void updateViewCap() {
        long newCap = songRepo.findViewCountPercentile90();

        if (newCap < 1) newCap = 1;

        this.cachedViewCap = (double) newCap;
        this.cachedLogCap = Math.log10(1 + this.cachedViewCap);

        log.info("Zaktualizowano ViewCap (Cache): {}", newCap);
    }

    /// Pojedynczy song trafia do metody a nastepnie:
    /// - wyliczena jest średnia waga (z wszystkich gatunków songa)
    /// - wyliczena jest waga dla autora
    /// - wyliczana jest 'waga wyswietlen',
    /// czyli jak duze znaczenie pod wzgledem wyswietlen ma song wzgledem reszty ( do pewnego momentu - cap z [ViewCapService] - 90 percentyl)
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


    /// Helpery

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


    /// Tworzy mape (Gatunek -> Waga Gatunku),
    /// gdzie waga kazdego gatunku wyliczana jest po tym jak duzy udzial dany gatunek ma w polubionych piosenkach użytkownika
    private Map<Genre, Double> calculateGenreProfile(List<Song> likedSongs, List<Song> dislikedSongs) {
        // na wszelki mimo ze w getRecoomendations wtedy i tak pojdzie topsongs
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty()) return Collections.emptyMap();

        Map<Genre, Double> scores = new HashMap<>();

        // liczy wagi dla konkretnych gatunkow po like
        for (Song song : likedSongs) {
            double contribution = 1.0 / song.getGenres().size();
            for (Genre g : song.getGenres()) {
                scores.merge(g, contribution, Double::sum);
            }
        }

        // liczy wagi ujemne dla konkretnych gatunkow po dislike
        for (Song song : dislikedSongs) {
            double contribution = (1.0 / song.getGenres().size()) * W_DISLIKE;
            for (Genre g : song.getGenres()) {
                scores.merge(g, -contribution, Double::sum);
            }
        }

        // normalizacja
        // bez tego z kazdym kolejnym like genre ma coraz wiekszy wplyw i moze prowadzic do - w zasadzie zepsucia algorytmu
        double totalActivityMass = likedSongs.size() + (dislikedSongs.size() * W_DISLIKE);

        if (totalActivityMass == 0) return Collections.emptyMap();

        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / totalActivityMass
                ));
    }

    private Map<Long, Double> calculateAuthorProfile(List<Song> likedSongs, List<Song> dislikedSongs) {
        // na wszelki mimo ze w getRecoomendations wtedy i tak pojdzie topsongs
        if (likedSongs.isEmpty() && dislikedSongs.isEmpty()) return Collections.emptyMap();

        Map<Long, Double> scores = new HashMap<>();

        // liczy wagi dla konkretnych autorow po like
        for (Song song : likedSongs) {
            scores.merge(song.getAuthor().getId(), 1.0, Double::sum);
        }

        // liczy wagi ujemne dla konkretnych autorow po dislike
        for (Song song : dislikedSongs) {
            scores.merge(song.getAuthor().getId(), -1.0 * W_DISLIKE, Double::sum);
        }

        // normalizacja
        // bez tego z kazdym kolejnym like author ma coraz wiekszy wplyw i moze prowadzic do - w zasadzie zepsucia algorytmu
        double totalActivityMass = likedSongs.size() + (dislikedSongs.size() * W_DISLIKE);

        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / totalActivityMass
                ));
    }


    // w sumie to mapa <Song, Double>
    private record ScoredSong(Song song, double score) {}
}