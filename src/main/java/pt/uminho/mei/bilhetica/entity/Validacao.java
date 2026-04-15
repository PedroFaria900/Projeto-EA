package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.enums.TipoEvento;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Validacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_id", nullable = false)
    private TituloTransporte titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragem_id", nullable = false)
    private Paragem paragem;

    @Column(nullable = false)
    private LocalDateTime momento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoValidacao resultado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;
}
