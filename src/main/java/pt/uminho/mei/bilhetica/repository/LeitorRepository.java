package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import java.util.UUID;

public interface LeitorRepository extends JpaRepository<Leitor, UUID> {
}