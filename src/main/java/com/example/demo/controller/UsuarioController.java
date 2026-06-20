package com.example.demo.controller;

import com.example.demo.model.Palpite;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PalpiteRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PalpiteRepository palpiteRepository; // INJETADO PARA O MOTOR DE DESEMPATE

    public UsuarioController(UsuarioRepository usuarioRepository, PalpiteRepository palpiteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.palpiteRepository = palpiteRepository;
    }

    // Método para LISTAR todos os usuários (GET)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // --- MOTOR DE RANKING E DESEMPATE MATEMÁTICO ---
    @GetMapping("/ranking")
    public ResponseEntity<List<Usuario>> verRanking() {
        
        // 1. Busca todos os usuários e os palpites de jogos já encerrados
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Palpite> palpitesValidos = palpiteRepository.findAllPalpitesComPartidaFinalizada();

        // 2. Agrupa palpites por ID do usuário (Performance extrema - evita loops no banco)
        Map<Long, List<Palpite>> palpitesPorUsuario = palpitesValidos.stream()
                .collect(Collectors.groupingBy(p -> p.getUsuario().getId()));

        // 3. Processa os critérios de desempate na memória
        for (Usuario u : usuarios) {
            int vencExato = 0, empExato = 0, vencMaisGolo = 0, venc = 0, umPlacar = 0;

            List<Palpite> meusPalpites = palpitesPorUsuario.getOrDefault(u.getId(), List.of());

            for (Palpite p : meusPalpites) {
                int golA_Pal = p.getGolosEquipaA();
                int golB_Pal = p.getGolosEquipaB();
                int golA_Real = p.getPartida().getGolosEquipaA();
                int golB_Real = p.getPartida().getGolosEquipaB();

                boolean realVitoriaA = golA_Real > golB_Real;
                boolean realVitoriaB = golB_Real > golA_Real;
                boolean realEmpate   = golA_Real == golB_Real;

                boolean acertouGolosA = golA_Pal == golA_Real;
                boolean acertouGolosB = golB_Pal == golB_Real;

                boolean palpiteVitoriaA = golA_Pal > golB_Pal;
                boolean palpiteVitoriaB = golB_Pal > golA_Pal;
                boolean palpiteEmpate   = golA_Pal == golB_Pal;

                boolean acertouVencedor = (palpiteVitoriaA && realVitoriaA) || 
                                          (palpiteVitoriaB && realVitoriaB) || 
                                          (palpiteEmpate && realEmpate);

                // Hierarquia exata discutida de Cravadas
                if (acertouVencedor && realEmpate && acertouGolosA && acertouGolosB) {
                    empExato++;
                } else if (acertouVencedor && !realEmpate && acertouGolosA && acertouGolosB) {
                    vencExato++;
                } else if (acertouVencedor && !realEmpate && (acertouGolosA || acertouGolosB)) {
                    vencMaisGolo++;
                } else if (acertouVencedor) {
                    venc++;
                } else if (!acertouVencedor && (acertouGolosA || acertouGolosB)) {
                    umPlacar++;
                }
            }

            // Injeta as estatísticas no usuário apenas para o Front-end
            u.setAcertosVencedorExato(vencExato);
            u.setAcertosEmpateExato(empExato);
            u.setAcertosVencedorMaisGolo(vencMaisGolo);
            u.setAcertosVencedor(venc);
            u.setAcertosUmPlacar(umPlacar);
        }

        // 4. O Algoritmo de Desempate Rigoroso
        usuarios.sort((u1, u2) -> {
            if (u1.getPontos() != u2.getPontos()) return Integer.compare(u2.getPontos(), u1.getPontos());
            if (u1.getAcertosVencedorExato() != u2.getAcertosVencedorExato()) return Integer.compare(u2.getAcertosVencedorExato(), u1.getAcertosVencedorExato());
            if (u1.getAcertosEmpateExato() != u2.getAcertosEmpateExato()) return Integer.compare(u2.getAcertosEmpateExato(), u1.getAcertosEmpateExato());
            if (u1.getAcertosVencedorMaisGolo() != u2.getAcertosVencedorMaisGolo()) return Integer.compare(u2.getAcertosVencedorMaisGolo(), u1.getAcertosVencedorMaisGolo());
            if (u1.getAcertosVencedor() != u2.getAcertosVencedor()) return Integer.compare(u2.getAcertosVencedor(), u1.getAcertosVencedor());
            if (u1.getAcertosUmPlacar() != u2.getAcertosUmPlacar()) return Integer.compare(u2.getAcertosUmPlacar(), u1.getAcertosUmPlacar());
            
            // Último recurso anti-caos: Ordem Alfabética
            if (u1.getNome() != null && u2.getNome() != null) {
                return u1.getNome().compareToIgnoreCase(u2.getNome());
            }
            return 0;
        });

        return ResponseEntity.ok(usuarios);
    }
}