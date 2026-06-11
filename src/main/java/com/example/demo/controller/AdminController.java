package com.example.demo.controller;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.CalculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CalculoService calculoService;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoPontuacaoRepository configRepository;

    public AdminController(CalculoService calculoService, UsuarioRepository usuarioRepository, ConfiguracaoPontuacaoRepository configRepository) {
        this.calculoService = calculoService;
        this.usuarioRepository = usuarioRepository;
        this.configRepository = configRepository;
    }

    private boolean isUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return false;
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Optional<Usuario> user = usuarioRepository.findByEmail(userDetails.getUsername());
        
        return user.isPresent() && user.get().isAdmin();
    }

    // --- ROTAS DE CONFIGURAÇÃO DE PONTOS (AGORA POR FASE) ---
    @GetMapping("/configuracao/{fase}")
    public ResponseEntity<?> getConfiguracaoDaFase(@PathVariable String fase) {
        if (!isUserAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        ConfiguracaoPontuacao config = configRepository.findById(fase).orElseGet(() -> {
            ConfiguracaoPontuacao padrao = new ConfiguracaoPontuacao();
            padrao.setFase(fase);
            padrao.setPontosPlacarExato(25);
            padrao.setPontosVencedor(10);
            padrao.setPontosGoloEquipa(5);
            return padrao;
        });
        
        return ResponseEntity.ok(config);
    }

    @PostMapping("/configuracao")
    public ResponseEntity<?> salvarConfiguracao(@RequestBody ConfiguracaoPontuacao novaConfig) {
        if (!isUserAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        if (novaConfig.getFase() == null || novaConfig.getFase().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"erro\": \"Fase não especificada.\"}");
        }

        configRepository.save(novaConfig);
        return ResponseEntity.ok("{\"mensagem\": \"Regras da fase atualizadas com sucesso!\"}");
    }

    // --- ROTA DE ENCERRAMENTO DE PARTIDA ---
    @PostMapping("/partidas/{id}/encerrar")
    public ResponseEntity<?> encerrarPartida(@PathVariable Long id, @RequestBody Map<String, Integer> placar) {
        if (!isUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"erro\": \"Acesso negado. Apenas Administradores.\"}");
        }

        Integer golosA = placar.get("golosEquipaA");
        Integer golosB = placar.get("golosEquipaB");

        if (golosA == null || golosB == null) {
            return ResponseEntity.badRequest().body("{\"erro\": \"Placar inválido.\"}");
        }

        try {
            calculoService.encerrarPartidaECalcularPontos(id, golosA, golosB);
            return ResponseEntity.ok("{\"mensagem\": \"Partida encerrada e pontos distribuídos com sucesso!\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }
}