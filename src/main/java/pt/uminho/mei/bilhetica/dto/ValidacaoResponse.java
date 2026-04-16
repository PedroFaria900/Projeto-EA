package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacaoResponse {
    private UUID validacaoId;
    private ResultadoValidacao resultado;
    private String mensagem;
    private UUID viagemId;
}
