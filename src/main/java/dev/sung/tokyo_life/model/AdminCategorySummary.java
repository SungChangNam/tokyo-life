package dev.sung.tokyo_life.model;

import java.time.OffsetDateTime;

public record AdminCategorySummary(
        Long id,
        String name,
        Integer displayOrder,
        Long postCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}