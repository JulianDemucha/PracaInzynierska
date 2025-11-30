package com.soundspace.repository;

import com.soundspace.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.soundspace.enums.Genre;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Album getAlbumById(Long id);

    List<Album> findAllByAuthorId(Long authorId);

    @Query("SELECT a FROM Album a JOIN a.genres g WHERE g = :genre")
    List<Album> findAllByGenre(@Param("genre") Genre genre);

    @Query("SELECT a FROM Album a " +
            "LEFT JOIN FETCH a.author " +
            "LEFT JOIN FETCH a.genres " +
            "LEFT JOIN FETCH a.coverStorageKey " +
            "WHERE a.publiclyVisible = true")
    List<Album> findAllWithDetails();
}
