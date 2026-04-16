package pt.uminho.mei.bilhetica.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.*;
import pt.uminho.mei.bilhetica.entity.leitor.*;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.*;
import pt.uminho.mei.bilhetica.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ValidacaoService {

    private final ValidacaoRepository validacaoRepository;
    private final ViagemRepository viagemRepository;
    private final LeitorRepository leitorRepository;
    private final ParagemRepository paragemRepository;
    private final TituloTransporteRepository tituloRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public ValidacaoService(ValidacaoRepository validacaoRepository,
                            ViagemRepository viagemRepository,
                            LeitorRepository leitorRepository,
                            ParagemRepository paragemRepository,
                            TituloTransporteRepository tituloRepository,
                            RedisTemplate<String, String> redisTemplate) {
        this.validacaoRepository = validacaoRepository;
        this.viagemRepository = viagemRepository;
        this.leitorRepository = leitorRepository;
        this.paragemRepository = paragemRepository;
        this.tituloRepository = tituloRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public ValidacaoResponse processar(ValidacaoRequest request) {

        // 1. Verificar token no Redis
        String tituloIdStr = redisTemplate.opsForValue()
            .get("token:" + request.getToken());

        if (tituloIdStr == null) {
            return ValidacaoResponse.builder()
                .resultado(ResultadoValidacao.INVALIDO)
                .mensagem("Token inválido ou expirado")
                .build();
        }

        // 2. Carregar título e leitor
        UUID tituloId = UUID.fromString(tituloIdStr);
        TituloTransporte titulo = tituloRepository.findById(tituloId)
            .orElseThrow(() -> new RuntimeException("Título não encontrado"));

        Leitor leitor = leitorRepository.findById(request.getLeitorId())
            .orElseThrow(() -> new RuntimeException("Leitor não encontrado"));

        // 3. Determinar paragem
        Paragem paragem = determinarParagem(leitor, request);

        // 4. Determinar tipo de evento pelo sentido do leitor
        TipoEvento tipoEvento = request.getTipoEvento() != null
            ? TipoEvento.valueOf(request.getTipoEvento().toUpperCase())
            : (leitor.getSentido() == SentidoLeitor.SAIDA
                ? TipoEvento.SAIDA : TipoEvento.ENTRADA);

        // 5. Verificar estado do título
        if (titulo.getEstado() != EstadoTitulo.ATIVO) {
            return criarValidacao(titulo, leitor, paragem,
                tipoEvento, ResultadoValidacao.INVALIDO,
                "Título não está activo", null);
        }

        // 6. Verificar validade por tipo de título
        ResultadoValidacao resultadoValidade =
            verificarValidade(titulo);
        if (resultadoValidade != ResultadoValidacao.VALIDO) {
            return criarValidacao(titulo, leitor, paragem,
                tipoEvento, resultadoValidade,
                "Título expirado", null);
        }

        // 7. Verificar saldo (PACK)
        if (titulo instanceof TituloPack pack) {
            if (pack.getViagensRestantes() <= 0) {
                return criarValidacao(titulo, leitor, paragem,
                    tipoEvento, ResultadoValidacao.SEM_SALDO,
                    "Pack sem viagens disponíveis", null);
            }
        }

        // 8. Verificar zona geográfica
        if (!verificarZona(titulo, paragem)) {
            return criarValidacao(titulo, leitor, paragem,
                tipoEvento, ResultadoValidacao.FORA_ZONA,
                "Paragem fora da área do título", null);
        }

        // 9. Processar entrada ou saída
        if (tipoEvento == TipoEvento.ENTRADA) {

            // Verificar viagem duplicada
            if (viagemRepository.findViagemAbertaPorTitulo(tituloId)
                    .isPresent()) {
                return criarValidacao(titulo, leitor, paragem,
                    tipoEvento, ResultadoValidacao.DUPLICADO,
                    "Já existe uma viagem em aberto", null);
            }

            // Decrementar pack
            if (titulo instanceof TituloPack pack) {
                pack.setViagensRestantes(pack.getViagensRestantes() - 1);
                if (pack.getViagensRestantes() == 0) {
                    pack.setEstado(EstadoTitulo.ESGOTADO);
                }
                tituloRepository.save(pack);
            }

            // Criar validação e viagem
            Validacao validacao = gravarValidacao(titulo, leitor,
                paragem, tipoEvento, ResultadoValidacao.VALIDO);

            Viagem viagem = Viagem.builder()
                .valEntrada(validacao)
                .inicio(LocalDateTime.now())
                .build();
            Viagem viagemSalva = viagemRepository.save(viagem);

            // Invalidar token no Redis
            redisTemplate.delete("token:" + request.getToken());

            return ValidacaoResponse.builder()
                .validacaoId(validacao.getId())
                .resultado(ResultadoValidacao.VALIDO)
                .mensagem("Boa viagem!")
                .viagemId(viagemSalva.getId())
                .build();

        } else {

            // Fechar viagem em aberto
            Viagem viagem = viagemRepository
                .findViagemAbertaPorTitulo(tituloId)
                .orElse(null);

            Validacao validacao = gravarValidacao(titulo, leitor,
                paragem, tipoEvento, ResultadoValidacao.VALIDO);

            if (viagem != null) {
                viagem.setValSaida(validacao);
                viagem.setFim(LocalDateTime.now());
                viagemRepository.save(viagem);
            }

            redisTemplate.delete("token:" + request.getToken());

            return ValidacaoResponse.builder()
                .validacaoId(validacao.getId())
                .resultado(ResultadoValidacao.VALIDO)
                .mensagem("Até à próxima!")
                .viagemId(viagem != null ? viagem.getId() : null)
                .build();
        }
    }

    private Paragem determinarParagem(Leitor leitor,
                                       ValidacaoRequest request) {
        if (leitor instanceof LeitorFixo fixo) {
            return fixo.getParagem();
        }
        // Leitor móvel — usa coordenadas do pedido
        // Por simplificação, retorna a primeira paragem disponível
        // Na implementação completa cruzaria com GPS + LinhaParagem
        return paragemRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Nenhuma paragem encontrada"));
    }

    private ResultadoValidacao verificarValidade(
            TituloTransporte titulo) {
        LocalDate hoje = LocalDate.now();
        LocalDate validade = null;

        if (titulo instanceof TituloPasse p) validade = p.getValidade();
        else if (titulo instanceof TituloPack p) validade = p.getValidade();
        else if (titulo instanceof TituloBilhete b) validade = b.getValidade();

        if (validade != null && validade.isBefore(hoje)) {
            titulo.setEstado(EstadoTitulo.EXPIRADO);
            tituloRepository.save(titulo);
            return ResultadoValidacao.INVALIDO;
        }
        return ResultadoValidacao.VALIDO;
    }

    private boolean verificarZona(TituloTransporte titulo,
                                   Paragem paragem) {
        String area = null;
        if (titulo instanceof TituloPasse p) area = p.getAreaGeografica();
        else if (titulo instanceof TituloPack p) area = p.getAreaGeografica();

        // Sem área definida = válido em toda a rede
        if (area == null || area.isBlank()) return true;

        // Simplificação — na implementação completa verificaria
        // se as coordenadas da paragem estão dentro do polígono
        return true;
    }

    private ValidacaoResponse criarValidacao(TituloTransporte titulo,
                                              Leitor leitor,
                                              Paragem paragem,
                                              TipoEvento tipoEvento,
                                              ResultadoValidacao resultado,
                                              String mensagem,
                                              UUID viagemId) {
        Validacao v = gravarValidacao(titulo, leitor, paragem,
            tipoEvento, resultado);
        return ValidacaoResponse.builder()
            .validacaoId(v.getId())
            .resultado(resultado)
            .mensagem(mensagem)
            .viagemId(viagemId)
            .build();
    }

    private Validacao gravarValidacao(TituloTransporte titulo,
                                       Leitor leitor,
                                       Paragem paragem,
                                       TipoEvento tipoEvento,
                                       ResultadoValidacao resultado) {
        Validacao v = Validacao.builder()
            .titulo(titulo)
            .leitor(leitor)
            .paragem(paragem)
            .momento(LocalDateTime.now())
            .tipoEvento(tipoEvento)
            .resultado(resultado)
            .build();
        return validacaoRepository.save(v);
    }
}
