package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String displayName,
        String email,
        UserRole role
) {
    public static AuthResponse bearer(String accessToken, User user) {
        return new AuthResponse("Bearer", accessToken, user.getId(), user.getDisplayName(), user.getEmail(),
                user.getRole());
    }
}
