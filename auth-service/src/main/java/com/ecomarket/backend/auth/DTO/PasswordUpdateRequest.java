package com.ecomarket.backend.auth.DTO;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
