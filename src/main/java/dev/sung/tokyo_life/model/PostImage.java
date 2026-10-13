package dev.sung.tokyo_life.model;

import java.time.OffsetDateTime;

public record PostImage(
        Long id,
        Long postId,
        String storageKey,
        String originalFilename,
        String contentType,
        Long fileSize,
        String altText,
        Integer displayOrder,
        Boolean isCover,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}