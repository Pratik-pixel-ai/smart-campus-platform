package com.smartcampus.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Flattened page wrapper. Spring's Page serialises with a lot of internal fields,
 * so the API returns this small, stable shape instead.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
