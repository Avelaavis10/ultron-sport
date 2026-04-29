package za.co.ultronsport.service;

import za.co.ultronsport.web.dto.AuthResponse;
import za.co.ultronsport.web.dto.CurrentUserResponse;
import za.co.ultronsport.web.dto.LoginRequest;
import za.co.ultronsport.web.dto.RegisterRequest;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    CurrentUserResponse currentUser(String email);
}
