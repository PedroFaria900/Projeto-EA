package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "utente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String telemovel; 

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;
}
