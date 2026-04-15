package pt.uminho.mei.bilhetica.entity.leitor;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoLeitor;
import pt.uminho.mei.bilhetica.enums.SentidoLeitor;
import java.util.UUID;

@Entity
@Table(name = "leitor")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLeitor tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SentidoLeitor sentido;
}
