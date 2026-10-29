package dev.sung.tokyo_life.model;

import java.util.List;

public record PostPage(
        List<PostListItem> posts,
        int currentPage,
        int pageSize,
        long totalCount,
        long totalPages
) {

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }
}