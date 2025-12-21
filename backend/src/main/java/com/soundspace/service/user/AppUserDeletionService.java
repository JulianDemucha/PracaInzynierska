package com.soundspace.service.user;

import com.soundspace.entity.AppUser;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.repository.*;
import com.soundspace.service.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserDeletionService {

    private final AppUserService appUserService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PlaylistEntryRepository playlistEntryRepository;
    private final PlaylistRepository playlistRepository;
    private final SongReactionRepository songReactionRepository;
    private final SongViewRepository songViewRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final AppUserRepository appUserRepository;
    private final StorageKeyRepository storageKeyRepository;
    private final StorageService storageService;

    // BULK DELETE -> 13 zapytań do bazy (jedno leci na komentarze, ktore prawdopodobnie zostana calkiem usuniete)
    @Transactional
    public void deleteUser(String requesterEmail) {
        AppUser appUser = appUserService.getUserByEmail(requesterEmail);
        Long appUserId = appUser.getId();

        // usuniecie wszystkich istniejacych w bazie refreshTokenow usera
        refreshTokenRepository.deleteAllByAppUserId(appUserId);

        // zapisanie playlist do naprawienia po usunieciu piosenek usera (beda dziury w pozycjach)
        List<Long> playlistsToRepair = playlistEntryRepository.findPlaylistIdsToRepair(appUserId);

        // usuniecie songow usera ze wszystkich playlist w ktorych sa jak i wszystkie piosenki z jego wlasnych playlist
        playlistEntryRepository.deleteEntriesBySongAuthorId(appUserId);

        // naprawa playlist po usunieciu songow
        playlistEntryRepository.renumberPlaylists(playlistsToRepair);

        // usunięcie wszystkich playlist usera
        playlistRepository.deleteAllByCreatorId(appUserId);

        // usuniecie wszystkich reakcji usera i reakcji dotyczacych piosenek usera
        songReactionRepository.deleteAllRelatedToUser(appUserId);

        // usuniecie wszystkich wyswietlen na piosenkach usera i odpiecie usera od jego wyswietlen na innych piosenkach
        songViewRepository.deleteAllBySongAuthorId(appUserId);
        songViewRepository.detachUserFromViews(appUserId);

        // usunięcie wszystkich piosenek i następnie albumów usera
        songRepository.deleteAllByAuthorId(appUserId);
        albumRepository.deleteAllByAuthorId(appUserId);


        // usuniecie samego usera
        appUserRepository.delete(appUser);
        appUserRepository.flush();

        // czyszczenie kontekstu tylko jezeli user usuwa sam siebie (metoda jest tez uzywana w deleteUserByAdmin)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName().equals(appUser.getEmail())) {
            SecurityContextHolder.clearContext();
        }

        // usuniecie wszystkich storageKeys powiazanych z userem
        storageKeyRepository.deleteAllByUserId(appUserId);

        // usuniecie wszystkich plikow powiazanych bezposrednio z userem (pliki piosenek, okladki albumow, piosenek, playlist itp.)
        storageService.deleteAllUserFiles(appUserId);

    }

    @Transactional
    public void deleteUserByAdmin(Long userId, String requesterEmail){
        AppUser sadUserSentencedForEternalDeletion = appUserService.getUserById(userId);
        AppUser requester = null;

        boolean isAdmin = false;
        if(requesterEmail != null) {
            requester = appUserService.getUserByEmail(requesterEmail);
            isAdmin = requester.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        if (requester != null && requester.getEmail().equals(sadUserSentencedForEternalDeletion.getEmail()))
            throw new IllegalArgumentException("Administrator nie moze usunac samego siebie w panelu usuwania usera");

        if (!isAdmin)
            throw new AccessDeniedException("Requestujacy uzytkownik nie jest administratorem");

        deleteUser(sadUserSentencedForEternalDeletion.getEmail());
    }
}
