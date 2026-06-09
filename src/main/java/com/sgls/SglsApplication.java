package com.sgls;

import com.sgls.entity.User;
import com.sgls.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SGLS APPLICATION — ENTRY POINT
 * --------------------------------
 * This is the main class that starts the entire Spring Boot application.
 *
 * @SpringBootApplication is a COMBINATION of three annotations:
 *   @Configuration       — this class can define @Bean methods
 *   @EnableAutoConfiguration — Spring Boot auto-configures based on classpath
 *                              (sees spring-boot-starter-web → auto-configures Tomcat)
 *                              (sees mysql-connector → auto-configures DataSource)
 *   @ComponentScan       — scans com.sgls and all sub-packages for:
 *                          @Component, @Service, @Repository, @Controller, @RestController
 *
 * INTERVIEW QUESTION: "What does SpringApplication.run() do?"
 *   1. Creates the Spring ApplicationContext (IoC container)
 *   2. Scans for @Component classes and instantiates them
 *   3. Wires dependencies (dependency injection)
 *   4. Starts embedded Tomcat on port 8080
 *   5. Registers all @RequestMapping routes
 *   6. Runs CommandLineRunner beans after startup
 *
 * INTERVIEW: "What is Inversion of Control (IoC)?"
 *   Normally, your code controls when objects are created: new MyService()
 *   With IoC, the SPRING CONTAINER creates and manages objects.
 *   You just declare what you need (@Autowired), Spring provides it.
 *   This is how @RequiredArgsConstructor + final fields work:
 *   Spring sees the constructor needs a UserRepository, so it provides one.
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class SglsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SglsApplication.class, args);
        log.info("==============================================");
        log.info("  Smart Global Logistics System — STARTED");
        log.info("  URL: http://localhost:8080");
        log.info("  API: http://localhost:8080/api/auth/login");
        log.info("==============================================");
    }

    /**
     * dataSeeder — runs ONCE after application startup.
     *
     * CommandLineRunner is a Spring Boot interface with a single method:
     *   run(String... args) — executed after the context is fully loaded.
     *
     * We use it to seed the admin user if the database is empty.
     * This ensures you can always log in on a fresh deployment.
     *
     * INTERVIEW: "How do you seed data in Spring Boot?"
     *   Options:
     *   1. CommandLineRunner (this approach) — Java code, type-safe
     *   2. data.sql in resources/ — runs raw SQL on startup
     *   3. Flyway/Liquibase — migration-based, version-controlled schema
     *
     * For production, Flyway is preferred. For this project, CommandLineRunner
     * keeps it simple and is easy to explain.
     */
    @Bean
    CommandLineRunner dataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only seed if no admin exists (prevents duplicate on restart)
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
                log.info("Default admin user seeded: admin / admin123");
            }

            if (!userRepository.existsByUsername("manager1")) {
                User manager = User.builder()
                        .username("manager1")
                        .email("manager@sgls.com")
                        .password(passwordEncoder.encode("manager123"))
                        .fullName("Warehouse Manager")
                        .role(User.Role.MANAGER)
                        .active(true)
                        .build();
                userRepository.save(manager);
                log.info("Default manager user seeded: manager1 / manager123");
            }

            if (!userRepository.existsByUsername("employee1")) {
                User employee = User.builder()
                        .username("employee1")
                        .email("employee@sgls.com")
                        .password(passwordEncoder.encode("employee123"))
                        .fullName("John Employee")
                        .role(User.Role.EMPLOYEE)
                        .active(true)
                        .build();
                userRepository.save(employee);
                log.info("Default employee user seeded: employee1 / employee123");
            }

            log.info("Data seeding complete.");
        };
    }
}
