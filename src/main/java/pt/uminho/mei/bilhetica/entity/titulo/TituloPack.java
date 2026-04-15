package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "titulo_pack")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloPack extends TituloTransporte {

    @Column(nullable = false)
    private LocalDate validade;

    @Column(nullable = false)
    private Integer viagensRestantes;

    private String areaGeografica;
}