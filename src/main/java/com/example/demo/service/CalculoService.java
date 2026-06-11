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

        // Busca a regra dinâmica (se não existir, cria a padrão)
        ConfiguracaoPontuacao regra = configRepository.findById("GLOBAL")
                .orElseGet(() -> {
                    ConfiguracaoPontuacao padrao = new ConfiguracaoPontuacao();
                    padrao.setFase("GLOBAL");
                    padrao.setPontosPlacarExato(25);
                    padrao.setPontosVencedor(10);
                    padrao.setPontosGoloEquipa(5); // A nova regra
                    return padrao;
                });

        for (Palpite p : palpitesDaPartida) {
            int pontos = 0;

            boolean acertouGolosA = p.getGolosEquipaA().equals(partida.getGolosEquipaA());
            boolean acertouGolosB = p.getGolosEquipaB().equals(partida.getGolosEquipaB());

            // LÓGICA EM CASCATA (Do prémio maior para o menor)
            if (acertouGolosA && acertouGolosB) {
                pontos = regra.getPontosPlacarExato();
            } 
            else if (isVencedorCorreto(p, partida)) {
                pontos = regra.getPontosVencedor();
            } 
            else if (acertouGolosA || acertouGolosB) {
                pontos = regra.getPontosGoloEquipa();
            }

            // Atualiza saldos
            if (pontos > 0) {
                p.setPontosGanhos(pontos);
                palpiteRepository.save(p);

                Usuario usuario = p.getUsuario();
                usuario.setPontos(usuario.getPontos() + pontos);
                usuarioRepository.save(usuario);
            } else {
                p.setPontosGanhos(0);
                palpiteRepository.save(p);
            }
        }
    }

    private boolean isVencedorCorreto(Palpite p, Partida part) {
        boolean palpiteVitoriaA = p.getGolosEquipaA() > p.getGolosEquipaB();
        boolean realVitoriaA = part.getGolosEquipaA() > part.getGolosEquipaB();

        boolean palpiteVitoriaB = p.getGolosEquipaA() < p.getGolosEquipaB();
        boolean realVitoriaB = part.getGolosEquipaA() < part.getGolosEquipaB();

        boolean palpiteEmpate = p.getGolosEquipaA().equals(p.getGolosEquipaB());
        boolean realEmpate = part.getGolosEquipaA().equals(part.getGolosEquipaB());

        return (palpiteVitoriaA && realVitoriaA) ||
               (palpiteVitoriaB && realVitoriaB) ||
               (palpiteEmpate && realEmpate);
    }
}