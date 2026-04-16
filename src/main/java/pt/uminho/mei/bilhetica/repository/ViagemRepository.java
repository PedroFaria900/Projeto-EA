package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Viagem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViagemRepository extends JpaRepository<Viagem, UUID> {

    @Query("""
        SELECT v FROM Viagem v
        WHERE v.valEntrada.titulo.id = :tituloId
        AND v.valSaida IS NULL
    """)
    Optional<Viagem> findViagemAbertaPorTitulo(
        @Param("tituloId") UUID tituloId);

    @Query("""
        SELECT v FROM Viagem v
        WHERE v.valEntrada.titulo.utente.id = :utenteId
        ORDER BY v.inicio DESC
    """)
    List<Viagem> findByUtenteId(@Param("utenteId") UUID utenteId);
}
