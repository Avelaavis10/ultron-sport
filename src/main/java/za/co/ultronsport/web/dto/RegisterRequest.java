package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.ultronsport.domain.UserRole;

public record RegisterRequest(
        @NotBlank String displayName,
        @Email @NotBlank String email,
        String phone,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotNull UserRole role
) {
}
