package com.soundspace.service;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.SongView;
import com.soundspace.repository.SongViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final SongViewRepository viewRepository;
    private final AppUserService appUserService;
    private final SongCoreService songCoreService;
    private final ViewBufferingService viewBufferingService;

    private static final int viewRegisterCooldownInSeconds = 60 * 15; // 15 min

    /*
        true gdy udalo sie dodac nowe wyswietlenie,
        false gdy istnieje już młodsze niż viewRegisterCooldownInSeconds (15 min)
     */
    @Transactional
    public boolean registerView(Long songId, UserDetails userDetails, String clientIp) {
        Instant cutoff = Instant.now().minusSeconds(viewRegisterCooldownInSeconds);
        AppUser appUser;

        if (userDetails != null) {
            appUser = appUserService.getUserByEmail(userDetails.getUsername());
            Long userId = appUser.getId();
            if (viewRepository.existsBySongIdAndUserIdAndViewedAtAfter(songId, userId, cutoff))
                return false; //istnieje juz

            // nowe wyswietlenie dla zalogowanego usera + ip przy okazji
            SongView view = new SongView();
            view.setSong(songCoreService.getReferenceById(songId));
            view.setIpAddress(clientIp);
            view.setUser(appUser);
            viewRepository.save(view);

        } else {
            // jezeli nie zalogowany, to sprawdzamy po ip, a jak nie udalo sie wyciagnac ip to nara łaski bez
            if (clientIp == null || clientIp.isBlank())
                return false; // brak ip - nie udalo sie zarejestrowac wyswietlenia

            if (viewRepository.existsBySongIdAndIpAddressAndViewedAtAfter(songId, clientIp, cutoff))
                return false; //istnieje juz

            // nowe wyswietlenie dla anonimowego usera o danym ip
            SongView view = new SongView();
            view.setSong(songCoreService.getReferenceById(songId));
            view.setIpAddress(clientIp);
            view.setUser(null);
            viewRepository.save(view);

        }

        // zapis like do sigma bufora zamiast bezposrednio do bazy
        viewBufferingService.bufferView(songId);
        return true;
    }
}