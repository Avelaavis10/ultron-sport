package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.ultronsport.domain.UserRole;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String displayName,
        @Email @NotBlank @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @NotBlank @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,
        @NotNull UserRole role
) {
}
