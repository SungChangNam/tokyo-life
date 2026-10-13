package dev.sung.tokyo_life.model;

public record StoredImage(
        String storageKey,
        String originalFilename,
        String contentType,
        Long fileSize
) {
}