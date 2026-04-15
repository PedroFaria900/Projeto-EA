package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "viagem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "val_entrada_id", nullable = false)
    private Validacao valEntrada;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "val_saida_id")
    private Validacao valSaida;

    @Column(nullable = false)
    private LocalDateTime inicio;

    private LocalDateTime fim;
}
