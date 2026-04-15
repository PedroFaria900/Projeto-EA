package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "titulo_transporte")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TituloTransporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column
    private EstadoTitulo estado;

    private String tokenAtivo;

    private LocalDateTime tokenExpiraEm;
}
