package pt.uminho.mei.bilhetica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistarRequest {
    private String nome;
    private String email;
    private String telemovel;
    private String password;
}
