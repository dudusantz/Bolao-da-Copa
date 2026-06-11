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

    // --- O RAIO-X DA SEGURANÇA ---
    private boolean isUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            System.out.println("❌ RAIO-X ADMIN: Bloqueado. Nenhuma sessão ativa encontrada. O utilizador não está logado.");
            return false;
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Optional<Usuario> user = usuarioRepository.findByEmail(userDetails.getUsername());
        
        if (user.isEmpty()) {
            System.out.println("❌ RAIO-X ADMIN: Bloqueado. O email " + userDetails.getUsername() + " não existe na base de dados.");
            return false;
        }
        
        if (!user.get().isAdmin()) {
            System.out.println("❌ RAIO-X ADMIN: Bloqueado. O utilizador " + user.get().getEmail() + " foi encontrado, mas a coluna 'is_admin' retornou FALSE no Java.");
            return false;
        }
        
        System.out.println("✅ RAIO-X ADMIN: Acesso Concedido com sucesso para: " + user.get().getEmail());
        return true;
    }

    // --- ROTAS DE CONFIGURAÇÃO DE PONTOS ---
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
        System.out.println(">>> TENTATIVA DE SALVAR REGRAS RECEBIDA PARA A FASE: " + novaConfig.getFase());
        
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"erro\": \"Falha de permissão.\"}");
            }
            
            if (novaConfig.getFase() == null || novaConfig.getFase().isEmpty()) {
                System.out.println("❌ ERRO: A fase enviada pelo Front-end está vazia.");
                return ResponseEntity.badRequest().body("{\"erro\": \"Fase não especificada.\"}");
            }

            configRepository.save(novaConfig);
            System.out.println("✅ REGRAS SALVAS NO BANCO COM SUCESSO!");
            return ResponseEntity.ok("{\"mensagem\": \"Regras da fase atualizadas com sucesso!\"}");
            
        } catch (Exception e) {
            System.out.println("🚨 ERRO CRÍTICO AO SALVAR NO BANCO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"erro\": \"Erro interno: " + e.getMessage() + "\"}");
        }
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