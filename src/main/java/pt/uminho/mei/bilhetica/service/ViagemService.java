package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.ViagemResponse;
import pt.uminho.mei.bilhetica.entity.Viagem;
import pt.uminho.mei.bilhetica.entity.leitor.LeitorFixo;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;
import pt.uminho.mei.bilhetica.repository.ViagemRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final UtenteRepository utenteRepository;

    public ViagemService(ViagemRepository viagemRepository,
                         UtenteRepository utenteRepository) {
        this.viagemRepository = viagemRepository;
        this.utenteRepository = utenteRepository;
    }

    public List<ViagemResponse> historico(String email) {
        var utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return viagemRepository.findByUtenteId(utente.getId())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ViagemResponse detalhe(UUID id) {
        return toResponse(viagemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Viagem não encontrada")));
    }

    private ViagemResponse toResponse(Viagem v) {
        String origem = null, destino = null, linha = null;

        if (v.getValEntrada() != null
                && v.getValEntrada().getParagem() != null) {
            origem = v.getValEntrada().getParagem().getNome();
        }
        if (v.getValSaida() != null
                && v.getValSaida().getParagem() != null) {
            destino = v.getValSaida().getParagem().getNome();
        }
        if (v.getValEntrada() != null
                && v.getValEntrada().getLeitor() instanceof LeitorFixo fixo
                && fixo.getParagem() != null) {
            linha = "N/A";
        }

        return ViagemResponse.builder()
            .id(v.getId())
            .inicio(v.getInicio())
            .fim(v.getFim())
            .paragemOrigem(origem)
            .paragemDestino(destino)
            .linha(linha)
            .build();
    }
}
