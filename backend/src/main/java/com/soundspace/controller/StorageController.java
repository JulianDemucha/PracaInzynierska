//package com.soundspace.controller;
//
//import com.soundspace.service.StorageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.Resource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/storage")
//@RequiredArgsConstructor
//public class StorageController {
//    private final StorageService storageService;
//
//    @GetMapping("/file/{storageKey}")
//    public ResponseEntity<Resource> getFile(@PathVariable String storageKey) {
//        return ResponseEntity.ok(storageService.loadAsResource(storageKey));
//    }
//}

//// zostawiam plik bo przy migracji na encje storagekey zamiast trzymac stringi na sztywno to tu sie pojawi ten endpoint
//// widoczny problem narazie to bylby bezposredni dostep do plikow /data wszystkich mimo ze tam nie ma raczej nic chronionego (poza piosenkami,
//// ale to mozna walidowac po tym czy jest prywatna) zreszta zawsze mozna tez calkiem rozpisac to bardziej bezpiecznie czy cos

//// StorageKey: id, key, mimeType