package dev.sung.tokyo_life.model;

import java.time.OffsetDateTime;

public record AdminTagSummary(
        Long id,
        String name,
        Long postCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}