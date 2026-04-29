package za.co.ultronsport.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // TODO: Add JWT authentication and token validation filter.
                // TODO: Enforce role-based access control for athlete, coach, organisation, scout/agent, and admin APIs.
                // TODO: Add rate limiting at the API gateway or filter layer.
                // TODO: Add POPIA/privacy compliance checks before exposing personal athlete data.
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // TODO: Tune password hashing parameters and add breached-password checks before production.
        return new BCryptPasswordEncoder();
    }
}
