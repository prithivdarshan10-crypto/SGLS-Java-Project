package com.sgls;

import com.sgls.entity.User;
import com.sgls.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sgls.repository")
@EntityScan(basePackages = "com.sgls.entity")
@RequiredArgsConstructor
public class SglsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SglsApplication.class, args);
    }

    @Bean
    CommandLineRunner dataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@sgls.com")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Administrator")
                        .role(User.Role.ADMIN)
                        .active(true)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
