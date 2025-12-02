package com.soundspace.service;

import com.soundspace.repository.SongReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
/*
    todo:
     - dodawanie like/dislike
     - dodawanie favourite
     - usuwanie liek/dislike/favourite
        *like i dislike nie mogą współistnieć, ale favourite jest niezależne

 */
public class LikeService {
    private final SongReactionRepository songReactionRepository;


}
