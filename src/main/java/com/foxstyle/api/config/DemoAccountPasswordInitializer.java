package com.foxstyle.api.config;

import com.foxstyle.api.entity.User;
import com.foxstyle.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

/** Keeps only the documented sample accounts in sync without reseeding business data. */
@Component
@RequiredArgsConstructor
public class DemoAccountPasswordInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        updatePassword("admin", "Admin123@");
        updatePassword("staff", "Staff123@");
        IntStream.rangeClosed(1, 10)
                .forEach(index -> updatePassword("customer" + index, "Chien123@"));
    }

    private void updatePassword(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(rawPassword));
            }
            user.setStatus((byte) 1);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        });
    }
}
