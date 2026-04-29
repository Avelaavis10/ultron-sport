package za.co.ultronsport.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UltronUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UltronUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required."))
                        .accessDeniedHandler((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/evidence").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.GET, "/api/evidence/my").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.PATCH, "/api/evidence/*").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.POST, "/api/evidence/*/submit").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.GET, "/api/evidence/pending-verification")
                        .hasAnyRole("COACH", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/evidence/*/verify").hasRole("COACH")
                        .requestMatchers(HttpMethod.POST, "/api/evidence/*/reject").hasRole("COACH")
                        .requestMatchers(HttpMethod.POST, "/api/evidence/*/flag").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/evidence/*/archive").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/evidence/*/verification-history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/evidence/*").authenticated()
                        .requestMatchers("/api/discovery/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/levelplay/me").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.POST, "/api/levelplay/recalculate-all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/levelplay/athletes/*/recalculate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/levelplay/**")
                        .hasAnyRole("ATHLETE", "COACH", "ORGANISATION", "SCOUT_AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/athlete-profiles")
                        .hasAnyRole("SCOUT_AGENT", "ADMIN", "COACH", "ORGANISATION")
                        .requestMatchers(HttpMethod.POST, "/api/v1/athlete-profiles").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/achievements").hasRole("ATHLETE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/coach-profiles").hasRole("COACH")
                        .requestMatchers("/api/v1/coach-profiles", "/api/v1/coach-profiles/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/organisations").hasAnyRole("ORGANISATION", "ADMIN")
                        .requestMatchers("/api/v1/organisations", "/api/v1/organisations/**")
                        .hasAnyRole("ORGANISATION", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/verification-requests").hasRole("ATHLETE")
                        .requestMatchers("/api/v1/verification-requests/*/approve",
                                "/api/v1/verification-requests/*/reject",
                                "/api/v1/verification-requests/*/flag")
                        .hasAnyRole("COACH", "ADMIN")
                        .requestMatchers("/api/v1/verification-requests", "/api/v1/verification-requests/**")
                        .hasAnyRole("COACH", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/levelplay-scores/athlete/*/refresh")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin", "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                // TODO: Add refresh tokens, password reset, email/phone verification, account lockout, MFA, and OAuth/social login.
                // TODO: Add rate limiting at the API gateway or filter layer.
                // TODO: Add POPIA/privacy controls before exposing sensitive athlete data.
                // TODO: Expand audit logging and admin moderation around security-sensitive events.
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // TODO: Tune password hashing parameters and add breached-password checks before production.
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
