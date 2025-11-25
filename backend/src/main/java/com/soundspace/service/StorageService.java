package com.soundspace.service;

import com.soundspace.entity.StorageKey;
import com.soundspace.exception.StorageException;
import com.soundspace.exception.StorageFileNotFoundException;
import com.soundspace.repository.StorageKeyRepository;
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
    private final StorageKeyRepository storageKeyRepository;

    public StorageService(@Value("${app.storage.root:./data/audio}") String rootStr, StorageKeyRepository storageKeyRepository) {
        try {
            this.rootPath = Paths.get(rootStr).toAbsolutePath().normalize();
            Files.createDirectories(this.rootPath); // tworzymy root, jak nie istnieje
        } catch (IOException e) {
            throw new StorageException("Nie można zainicjować folderu storage", e);
        }
        this.storageKeyRepository = storageKeyRepository;
    }

    /**
     * plik source -> storage i zwraca storageKey czyli path gdzie jest
     */
    public String saveFromPath(Path source, Long ownerId, String extension, String subDirectory) throws IOException {
        String key = String.format("%s/%d/%s.%s", subDirectory, ownerId == null ? 0 : ownerId, UUID.randomUUID(), extension);
        Path target = rootPath.resolve(key).normalize();
        Files.createDirectories(target.getParent());
        try {
            Files.createDirectories(target.getParent());
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return key;
        } catch (IOException e) {
            throw new StorageException("Nie udało się zapisać pliku w storage", e);
        }
    }

    public Resource loadAsResource(String storageKey) {
        try {
            Path file = rootPath.resolve(storageKey).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Nie znaleziono pliku: " + storageKey);
            }

        } catch (IOException e) {
            throw new StorageFileNotFoundException(e.getMessage());
        }
    }

    public Path resolvePath(String storageKey) {
        return rootPath.resolve(storageKey).normalize();
    }

    public void delete(String storageKey) throws IOException {
        Path file = resolvePath(storageKey);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Nie udało się usunąć pliku: " + storageKey, e);
        }
    }

    public StorageKey getStorageKey(Long storageKeyId) {
        return storageKeyRepository.findById(storageKeyId).orElseThrow(
                () -> new StorageFileNotFoundException("Nie znaleziono pliku o storageKeyId: " + storageKeyId)
        );

    }

}
