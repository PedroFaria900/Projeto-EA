package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "paragem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paragem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

   @Column(nullable = false, unique = true)
    private String codigo;

    private String municipio;
    
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}