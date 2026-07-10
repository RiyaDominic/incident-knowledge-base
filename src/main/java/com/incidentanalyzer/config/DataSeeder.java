package com.incidentanalyzer.config;

import com.incidentanalyzer.model.User;
import com.incidentanalyzer.model.UserRole;
import com.incidentanalyzer.repository.UserRepository;
import com.incidentanalyzer.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    @Override
    public void run(String... args) {
        if (!bootstrapAdminProperties.enabled()) {
            return;
        }
        if (!StringUtils.hasText(bootstrapAdminProperties.email()) || !StringUtils.hasText(bootstrapAdminProperties.password())) {
            log.warn("Bootstrap admin is enabled but email/password are missing; skipping seed");
            return;
        }
        if (userRepository.existsByEmail(bootstrapAdminProperties.email().toLowerCase())) {
            return;
        }
        userService.createUser(User.builder()
                .name(StringUtils.hasText(bootstrapAdminProperties.name()) ? bootstrapAdminProperties.name() : "Bootstrap Admin")
                .email(bootstrapAdminProperties.email().toLowerCase())
                .password(bootstrapAdminProperties.password())
                .role(UserRole.ADMIN)
                .build());
        log.info("Bootstrap admin account created for {}", bootstrapAdminProperties.email());
    }
}
