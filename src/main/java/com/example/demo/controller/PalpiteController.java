package com.example.demo.controller;

import com.example.demo.model.Palpite;
import com.example.demo.model.Partida;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PalpiteRepository;
import com.example.demo.repository.PartidaRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/palpites")
public class PalpiteController {

    private final PalpiteRepository palpiteRepository;
    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;

    public PalpiteController(PalpiteRepository palpiteRepository, PartidaRepository partidaRepository, UsuarioRepository usuarioRepository) {
        this.palpiteRepository = palpiteRepository;
        this.partidaRepository = partidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/meus")
    public ResponseEntity<?> getMeusPalpites() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Não autenticado\"}");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(userDetails.getUsername());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Utilizador não encontrado\"}");
        }

        List<Palpite> meusPalpites = palpiteRepository.findByUsuarioId(usuarioOpt.get().getId());
        return ResponseEntity.ok(meusPalpites);
    }

    @GetMapping
    public List<Palpite> listarTodosPalpites() {
        return palpiteRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> salvarOuAtualizarPalpite(@RequestBody Palpite palpiteRequest) {
        try {
            // 1. SEGURANÇA: Autenticação via Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Sessão expirada. Faça login novamente.\"}");
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(userDetails.getUsername());
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Conta não localizada.\"}");
            }
            Usuario usuarioLogado = usuarioOpt.get();

            // 2. REGRAS DA PARTIDA
            Optional<Partida> partidaOptional = partidaRepository.findById(palpiteRequest.getPartida().getId());
            if (partidaOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Partida não encontrada no sistema.\"}");
            }
            Partida partida = partidaOptional.get();

            if (partida.isFinalizada()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"erro\": \"Aposta bloqueada! O jogo já foi encerrado.\"}");
            }

            // O SEGREDO: Forçar o relógio para o Fuso Horário de Brasília (BRT)
            ZoneId fusoBrasilia = ZoneId.of("America/Sao_Paulo");
            if (partida.getDataHoraDoJogo() != null && LocalDateTime.now(fusoBrasilia).isAfter(partida.getDataHoraDoJogo())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"erro\": \"Aposta bloqueada! O jogo já começou.\"}");
            }

            // 3. INTELIGÊNCIA DE UPSERT E AUTO-CURA (Lidando com os duplicados antigos)
            List<Palpite> palpitesExistentes = palpiteRepository.findByUsuarioIdAndPartidaId(usuarioLogado.getId(), partida.getId());
            
            Palpite palpiteFinal;

            if (!palpitesExistentes.isEmpty()) {
                // É UM UPDATE: Pega o primeiro registo da lista
                palpiteFinal = palpitesExistentes.get(0);
                palpiteFinal.setGolosEquipaA(palpiteRequest.getGolosEquipaA());
                palpiteFinal.setGolosEquipaB(palpiteRequest.getGolosEquipaB());
                
                // Limpeza de lixo: Se o código antigo gerou duplicações, apaga os extras
                if (palpitesExistentes.size() > 1) {
                    for (int i = 1; i < palpitesExistentes.size(); i++) {
                        palpiteRepository.delete(palpitesExistentes.get(i));
                    }
                }
            } else {
                // É UM INSERT: Cria um palpite novo e blinda as relações
                palpiteFinal = new Palpite();
                palpiteFinal.setUsuario(usuarioLogado);
                palpiteFinal.setPartida(partida);
                palpiteFinal.setGolosEquipaA(palpiteRequest.getGolosEquipaA());
                palpiteFinal.setGolosEquipaB(palpiteRequest.getGolosEquipaB());
                palpiteFinal.setPontosGanhos(0);
            }

            // 4. Salva o resultado limpo
            Palpite palpiteSalvo = palpiteRepository.save(palpiteFinal);
            return ResponseEntity.status(HttpStatus.OK).body(palpiteSalvo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"erro\": \"Erro interno do servidor: " + e.getMessage() + "\"}");
        }
    }
}