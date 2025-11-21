package com.soundspace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootPath;

    public StorageService(@Value("${app.storage.root:./data/audio}") String rootStr) {
        this.rootPath = Paths.get(rootStr).toAbsolutePath().normalize();
    }

    /**  plik source -> storage i zwraca storageKey czyli path gdzie jest */
    public String saveFromPath(Path source, Long ownerId, String extension, String subDirectory) throws IOException {
        String key = String.format("%s/%d/%s.%s",subDirectory, ownerId == null ? 0 : ownerId, UUID.randomUUID(), extension);
        Path target = rootPath.resolve(key).normalize();
        Files.createDirectories(target.getParent());
        try {            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // fallback
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return key;
    }

    public Resource loadAsResource(String storageKey) throws IOException {
        Path file = rootPath.resolve(storageKey).normalize();
        if (!Files.exists(file) || !Files.isReadable(file)) {
            throw new NoSuchFileException(file.toString());
        }
        return new UrlResource(file.toUri());
    }

    public Path resolvePath(String storageKey) {
        return rootPath.resolve(storageKey).normalize();
    }

    public boolean delete(String storageKey) throws IOException {
        Path file = resolvePath(storageKey);
        return Files.deleteIfExists(file);
    }
}
