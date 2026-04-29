package za.co.ultronsport.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.config.security.JwtService;
import za.co.ultronsport.service.AuthenticationService;
import za.co.ultronsport.web.dto.AuthResponse;
import za.co.ultronsport.web.dto.CurrentUserResponse;
import za.co.ultronsport.web.dto.LoginRequest;
import za.co.ultronsport.web.dto.RegisterRequest;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A user with this email already exists.");
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.create(request.displayName(), request.email(), request.phone(), passwordHash, request.role());
        // Local MVP accounts are active immediately. TODO: Add email/phone verification before production.
        user.activate();
        User saved = userRepository.save(user);
        return AuthResponse.bearer(jwtService.generateToken(saved), saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return AuthResponse.bearer(jwtService.generateToken(user), user);
    }

    @Override
    public CurrentUserResponse currentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return CurrentUserResponse.from(user);
    }
}
