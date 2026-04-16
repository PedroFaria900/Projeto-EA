package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacaoRequest {
    private String token;
    private UUID leitorId;
    private String tipoEvento;
    private Double latitude;
    private Double longitude;
}
