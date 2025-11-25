package com.soundspace.repository;

import com.soundspace.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Album getAlbumById(Long id);

    List<Album> findAllByAuthorId(Long authorId);
}
