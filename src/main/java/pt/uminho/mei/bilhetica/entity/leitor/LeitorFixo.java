package pt.uminho.mei.bilhetica.entity.leitor;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Paragem;

@Entity
@Table(name = "leitor_fixo")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeitorFixo extends Leitor {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragem_id", nullable = false)
    private Paragem paragem;

    private Double latitude;
    private Double longitude;
}