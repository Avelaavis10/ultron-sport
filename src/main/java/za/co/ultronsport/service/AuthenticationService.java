package za.co.ultronsport.service;

import za.co.ultronsport.domain.User;
import za.co.ultronsport.web.dto.RegisterUserRequest;

public interface AuthenticationService {
    User register(RegisterUserRequest request);
}
