package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.AccountStatus;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;

public record CurrentUserResponse(
        Long id,
        String displayName,
        String email,
        UserRole role,
        AccountStatus status
) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole(),
                user.getStatus());
    }
}
