package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "titulo_passe")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloPasse extends TituloTransporte {

    @Column(nullable = false)
    private LocalDate validade;

    private String areaGeografica;
}