package dev.sung.tokyo_life.model;

import java.time.OffsetDateTime;

public class AdminPostSummary {

    private final Long id;
    private final String title;
    private final String status;
    private final OffsetDateTime createdAt;

    public AdminPostSummary(
            Long id,
            String title,
            String status,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}