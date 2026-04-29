package za.co.ultronsport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.impl.AuthenticationServiceImpl;
import za.co.ultronsport.web.dto.RegisterUserRequest;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void registerHashesPasswordAndSavesUser() {
        RegisterUserRequest request = new RegisterUserRequest("Avela", "avela@example.com", "123",
                "password123", UserRole.ATHLETE);
        when(userRepository.existsByEmailIgnoreCase("avela@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authenticationService.register(request);

        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(UserRole.ATHLETE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterUserRequest request = new RegisterUserRequest("Avela", "avela@example.com", null,
                "password123", UserRole.ATHLETE);
        when(userRepository.existsByEmailIgnoreCase("avela@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
