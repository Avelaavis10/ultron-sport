package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.config.security.JwtService;
import za.co.ultronsport.domain.AccountStatus;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.impl.AuthenticationServiceImpl;
import za.co.ultronsport.web.dto.AuthResponse;
import za.co.ultronsport.web.dto.RegisterRequest;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void registerHashesPasswordSavesActiveUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Avela", "avela@example.com", "123",
                "password123", UserRole.ATHLETE);
        when(userRepository.existsByEmailIgnoreCase("avela@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authenticationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(saved.getRole()).isEqualTo(UserRole.ATHLETE);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(UserRole.ATHLETE);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Avela", "avela@example.com", null,
                "password123", UserRole.ATHLETE);
        when(userRepository.existsByEmailIgnoreCase("avela@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
