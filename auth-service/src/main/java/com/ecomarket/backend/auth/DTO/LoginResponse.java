package com.ecomarket.backend.auth.DTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    public LoginResponse(String token) {
        this.token = token;
    }
    private String token;
    private String tokenType = "Bearer";
}
