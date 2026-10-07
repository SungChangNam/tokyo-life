package dev.sung.tokyo_life;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = System.getenv("ADMIN_INITIAL_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "ADMIN_INITIAL_PASSWORD를 설정해주세요."
            );
        }

        String savedHash = "$2a$10$0F1BMuErhsCsCUxV8EAZxuKBfTIREOQLUvwQpSzewwnT3qrq.cWbi";

        boolean matches = new BCryptPasswordEncoder()
                .matches(password, savedHash);

        System.out.println("비밀번호 일치 여부: " + matches);
    }
}