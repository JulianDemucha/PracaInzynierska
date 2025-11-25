package com.soundspace.repository;

import com.soundspace.entity.StorageKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageKeyRepository extends JpaRepository<StorageKey, Long> {
}
