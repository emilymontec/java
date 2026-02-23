package com.bank.atlasbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/**", "/panel/**", "/health", "/error/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("EXECUTIVE")
                        .requestMatchers("/transactions/**", "/accounts/**").hasAnyRole("CUSTOMER", "EXECUTIVE")
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public UserDetailsService users() {
        Map<String, String> env = loadEmergencyAdminEnv();
        String username = env.getOrDefault("EMERGENCY_ADMIN_USERNAME", "./chief");
        String password = env.get("EMERGENCY_ADMIN_PASSWORD");
        String role = env.getOrDefault("EMERGENCY_ADMIN_ROLE", "EXECUTIVE");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EMERGENCY_ADMIN_PASSWORD must be set in .env for emergency admin user");
        }

        UserDetails emergencyAdmin = User.withDefaultPasswordEncoder()
                .username(username)
                .password(password)
                .roles(role)
                .build();

        return new InMemoryUserDetailsManager(emergencyAdmin);
    }

    private Map<String, String> loadEmergencyAdminEnv() {
        Path envPath = Path.of(".env");
        Map<String, String> map = new HashMap<>();

        if (!Files.exists(envPath)) {
            return map;
        }

        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                map.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file for emergency admin configuration", e);
        }

        return map;
    }
}
