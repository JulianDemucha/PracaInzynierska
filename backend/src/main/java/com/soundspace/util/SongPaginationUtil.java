package com.soundspace.util; // lub gdzie wolisz

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SongPaginationUtil {

    public static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        Objects.requireNonNull(list, "Lista nie może być pusta");
        Objects.requireNonNull(pageable, "Pageable nie może być pusty");

        int total = list.size();
        int start = (int) pageable.getOffset();

        if (start >= total) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        int end = Math.min(start + pageable.getPageSize(), total);
        // subList zwraca widok - opakowanie w arraylist zeby skopiowac
        List<T> pageContent = new ArrayList<>(list.subList(start, end));

        return new PageImpl<>(pageContent, pageable, total);
    }
}
