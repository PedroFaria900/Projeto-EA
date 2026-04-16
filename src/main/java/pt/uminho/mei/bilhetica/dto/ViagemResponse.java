package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViagemResponse {
    private UUID id;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private String paragemOrigem;
    private String paragemDestino;
    private String linha;
}
