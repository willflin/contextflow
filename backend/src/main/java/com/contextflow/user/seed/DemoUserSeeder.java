package com.contextflow.user.seed;

import com.contextflow.auth.domain.UserRole;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserStatus;
import com.contextflow.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoUserSeeder implements ApplicationRunner {

    private final boolean seedDemoUsers;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(
            @Value("${contextflow.auth.seed-demo-users:true}") boolean seedDemoUsers,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.seedDemoUsers = seedDemoUsers;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoUsers) {
            return;
        }

        createIfMissing("learner", "learner123", "Demo Learner", UserRole.LEARNER);
        createIfMissing("admin", "admin123", "Demo Admin", UserRole.ADMIN);
    }

    private void createIfMissing(String username, String rawPassword, String displayName, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(new UserEntity(
                username,
                passwordEncoder.encode(rawPassword),
                displayName,
                role,
                UserStatus.ACTIVE
        ));
    }
}
