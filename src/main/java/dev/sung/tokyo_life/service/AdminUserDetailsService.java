package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.mapper.UserMapper;
import dev.sung.tokyo_life.model.AdminUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public AdminUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AdminUser admin = userMapper.findByUsername(username);

        if (admin == null) {
            throw new UsernameNotFoundException(
                    "사용자를 찾을 수 없습니다."
            );
        }

        return User.withUsername(admin.getUsername())
                .password(admin.getPasswordHash())
                .roles("ADMIN")
                .disabled(
                        "!LOGIN_DISABLED!".equals(admin.getPasswordHash())
                )
                .build();
    }
}