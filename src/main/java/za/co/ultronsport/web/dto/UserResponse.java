package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.AccountStatus;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;

public record UserResponse(
        Long id,
        String displayName,
        String email,
        String phone,
        UserRole role,
        AccountStatus status
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getStatus());
    }
}
