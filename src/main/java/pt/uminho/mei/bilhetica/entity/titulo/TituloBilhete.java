package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Paragem;
import java.time.LocalDate;

@Entity
@Table(name = "titulo_bilhete")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloBilhete extends TituloTransporte {

    @Column(nullable = false)
    private LocalDate validade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragem_origem_id")
    private Paragem paragemOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragem_destino_id")
    private Paragem paragemDestino;
}
