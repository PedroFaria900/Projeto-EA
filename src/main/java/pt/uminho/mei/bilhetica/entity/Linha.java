package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import java.util.UUID;

@Entity
@Table(name = "linha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Linha {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String designacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransporte tipoTransporte;
}
