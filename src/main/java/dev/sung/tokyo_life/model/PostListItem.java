package dev.sung.tokyo_life.model;

public record PostListItem(
        Long id,
        String title,
        String summary,
        Long coverImageId,
        String coverAltText
) {
}