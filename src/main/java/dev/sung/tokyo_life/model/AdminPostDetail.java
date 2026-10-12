package dev.sung.tokyo_life.model;

public record AdminPostDetail(
        Long id,
        String title,
        String summary,
        Long categoryId,
        String content,
        String status
) {
}