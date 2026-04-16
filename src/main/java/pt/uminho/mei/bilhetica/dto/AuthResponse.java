package pt.uminho.mei.bilhetica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tipo = "Bearer";
    private long expiresIn;
}
