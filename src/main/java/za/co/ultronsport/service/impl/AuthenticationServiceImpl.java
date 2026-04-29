package za.co.ultronsport.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.DuplicateResourceException;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.AuthenticationService;
import za.co.ultronsport.web.dto.RegisterUserRequest;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A user with this email already exists.");
        }
        String passwordHash = passwordEncoder.encode(request.rawPassword());
        User user = User.create(request.displayName(), request.email(), request.phone(), passwordHash, request.role());
        return userRepository.save(user);
    }
}
