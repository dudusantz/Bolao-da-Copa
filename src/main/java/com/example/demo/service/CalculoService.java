package com.example.demo.service;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.model.Palpite;
import com.example.demo.model.Partida;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import com.example.demo.repository.PalpiteRepository;
import com.example.demo.repository.PartidaRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalculoService {

    private final PartidaRepository partidaRepository;
    private final PalpiteRepository palpiteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoPontuacaoRepository configRepository;

    public CalculoService(PartidaRepository partidaRepository,
                          PalpiteRepository palpiteRepository, 
                          UsuarioRepository usuarioRepository, 
                          ConfiguracaoPontuacaoRepository configRepository) {
        this.partidaRepository = partidaRepository;
        this.palpiteRepository = palpiteRepository;
        this.usuarioRepository = usuarioRepository;
        this.configRepository = configRepository;
    }

    @Transactional
    public void encerrarPartidaECalcularPontos(Long partidaId, int golosA_Real, int golosB_Real) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));
        
        partida.setGolosEquipaA(golosA_Real);
        partida.setGolosEquipaB(golosB_Real);
        partida.setFinalizada(true);
        partidaRepository.save(partida);

        calcularPontos(partida);
    }

    @Transactional
    public void calcularPontos(Partida partida) {
        List<Palpite> palpitesDaPartida = palpiteRepository.findByPartidaId(partida.getId());

        if (palpitesDaPartida.isEmpty()) return;

        int golA_Real = partida.getGolosEquipaA();
        int golB_Real = partida.getGolosEquipaB();
        
        boolean realVitoriaA = golA_Real > golB_Real;
        boolean realVitoriaB = golB_Real > golA_Real;
        boolean realEmpate   = golA_Real == golB_Real;

        // 1. O Tradutor: Descobre a regra desta fase
        String chaveFase = obterChaveDeConfiguracao(partida.getFase());

        // 2. Busca a regra dinâmica configurada pelo Admin
        ConfiguracaoPontuacao regra = configRepository.findById(chaveFase)
                .orElseGet(() -> {
                    ConfiguracaoPontuacao padrao = new ConfiguracaoPontuacao();
                    padrao.setFase(chaveFase);
                    padrao.setPontosEmpateExato(9);
                    padrao.setPontosVencedorExato(7);
                    padrao.setPontosVencedorMaisUmGolo(5);
                    padrao.setPontosVencedor(3);
                    padrao.setPontosGoloPerdedor(1);
                    return padrao;
                });

        for (Palpite p : palpitesDaPartida) {
            int pontos = 0;

            int golA_Palpite = p.getGolosEquipaA();
            int golB_Palpite = p.getGolosEquipaB();

            // Comparações individuais
            boolean acertouGolosA = golA_Palpite == golA_Real;
            boolean acertouGolosB = golB_Palpite == golB_Real;

            // Comparações de resultado
            boolean palpiteVitoriaA = golA_Palpite > golB_Palpite;
            boolean palpiteVitoriaB = golB_Palpite > golA_Palpite;
            boolean palpiteEmpate   = golA_Palpite == golB_Palpite;

            // Validação principal
            boolean acertouVencedor = (palpiteVitoriaA && realVitoriaA) || 
                                      (palpiteVitoriaB && realVitoriaB) || 
                                      (palpiteEmpate && realEmpate);

            // --- LÓGICA EM CASCATA COM VALORES DINÂMICOS ---

            // NÍVEL 1: Empate em cheio
            if (acertouVencedor && realEmpate && acertouGolosA && acertouGolosB) {
                pontos = regra.getPontosEmpateExato();
            }
            // NÍVEL 2: Resultado do vitorioso em cheio
            else if (acertouVencedor && !realEmpate && acertouGolosA && acertouGolosB) {
                pontos = regra.getPontosVencedorExato();
            }
            // NÍVEL 3: Vencedor correto + 1 placar correto
            else if (acertouVencedor && !realEmpate && (acertouGolosA || acertouGolosB)) {
                pontos = regra.getPontosVencedorMaisUmGolo();
            }
            // NÍVEL 4: Acertou Vencedor/Empate apenas
            else if (acertouVencedor) {
                pontos = regra.getPontosVencedor();
            }
            // NÍVEL 5: Errou o resultado geral, mas acertou os golos exatos de pelo menos UMA das equipas
            else if (!acertouVencedor && (acertouGolosA || acertouGolosB)) {
                pontos = regra.getPontosGoloPerdedor();
            }

            // --- ATUALIZAÇÃO DOS SALDOS NO BANCO (Reposicionado para dentro do loop) ---
            p.setPontosGanhos(pontos);
            palpiteRepository.save(p);

            if (pontos > 0) {
                Usuario usuario = p.getUsuario();
                usuario.setPontos(usuario.getPontos() + pontos);
                usuarioRepository.save(usuario);
            }
        }
    }

    // --- TRADUTOR DE FASES ---
    private String obterChaveDeConfiguracao(String faseDaPartida) {
        if (faseDaPartida == null) return "Fase de Grupos";
        
        String faseUpper = faseDaPartida.toUpperCase();
        
        if (faseUpper.contains("GRUPO")) return "Fase de Grupos";
        if (faseUpper.contains("16 AVOS")) return "16 Avos de Final";
        if (faseUpper.contains("OITAVA")) return "Oitavas de Final";
        if (faseUpper.contains("QUARTA")) return "Quartas de Final";
        if (faseUpper.contains("SEMI")) return "Semifinais";
        if (faseUpper.contains("FINAL")) return "Final";
        
        return "Fase de Grupos"; 
    }
}