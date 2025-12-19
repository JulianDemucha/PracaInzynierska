package com.soundspace.controller;

import com.soundspace.enums.Genre;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    @GetMapping
    public ResponseEntity<Genre[]> getGenres(){
        return ResponseEntity.ok(Genre.values());
    }
}
