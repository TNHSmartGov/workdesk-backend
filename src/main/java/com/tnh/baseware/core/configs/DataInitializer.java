package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.entities.user.Role;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.properties.InitProperties;
import com.tnh.baseware.core.repositories.user.IRoleRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(name = "baseware.core.system.initialized-enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    IUserRepository userRepository;
    IRoleRepository roleRepository;
    InitProperties initProperties;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initUsers();
    }

    void initRoles() {
        log.info("Initializing roles...");
        var existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        var rolesToSave = initProperties.getRoles()
                .stream()
                .filter(g -> !existingRoles.contains(g.getName()))
                .map(g -> Role.builder()
                        .name(g.getName())
                        .description(g.getDescription())
                        .build())
                .toList();

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAllAndFlush(rolesToSave);
        }
    }

    void initUsers() {
        log.info("Initializing users...");
        var existingUsernames = userRepository.findAll()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        var usersToSave = initProperties.getUsers()
                .stream()
                .filter(u -> !existingUsernames.contains(u.getUsername()))
                .map(u -> {
                    List<Role> roles = roleRepository.findAllByField("name", u.getRole());
                    if (roles.isEmpty()) return null;
                    return User.builder()
                            .username(u.getUsername())
                            .password(passwordEncoder.encode(u.getPassword()))
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .fullName(u.getFullName())
                            .phone(u.getPhone())
                            .email(u.getEmail())
                            .avatarUrl(u.getAvatarUrl())
                            .idn(u.getIdn())
                            .ial(u.getIal())
                            .enabled(u.isEnabled())
                            .locked(false)
                            .accountExpiryDate(Instant.now().plus(365, ChronoUnit.DAYS))
                            .failedLoginAttempts(0)
                            .roles(Set.of(roles.getFirst()))
                            .superAdmin(false)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        if (!usersToSave.isEmpty()) {
            userRepository.saveAllAndFlush(usersToSave);
        }
    }
}
