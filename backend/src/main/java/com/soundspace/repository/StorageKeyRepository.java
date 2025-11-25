package com.soundspace.repository;

import com.soundspace.entity.StorageKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageKeyRepository extends JpaRepository<StorageKey, Long> {
}
