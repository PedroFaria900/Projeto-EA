package pt.uminho.mei.bilhetica.entity.leitor;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Linha;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import java.time.LocalDateTime;

@Entity
@Table(name = "leitor_movel")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeitorMovel extends Leitor {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id")
    private Linha linha;

    private Double latitudeActual;
    private Double longitudeActual;

    private LocalDateTime ultimaActualizacao;

    @Enumerated(EnumType.STRING)
    private EstadoTitulo estado;
}
