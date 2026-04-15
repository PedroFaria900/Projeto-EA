package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;

@Entity
@Table(name = "linha_paragem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinhaParagem {
    
    @EmbeddedId
    private LinhaParagemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("linhaId")
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("paragemId")
    @JoinColumn(name = "paragem_id", nullable = false)
    private Paragem paragem;

    @Column(nullable = false)
    private Integer sequencia;

    private Integer distanciaMetros;

    private Integer tempoEstimadoSeg;
}
