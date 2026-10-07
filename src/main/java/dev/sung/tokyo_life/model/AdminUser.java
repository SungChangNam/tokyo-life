package dev.sung.tokyo_life.model;

public class AdminUser {
    private final Long id;
    private final String username;
    private final String passwordHash;

    public AdminUser(Long id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
