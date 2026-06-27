package com.example.demo.controller;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.model.Palpite;
import com.example.demo.model.Partida;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import com.example.demo.repository.PalpiteRepository;
import com.example.demo.repository.PartidaRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.CalculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CalculoService calculoService;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoPontuacaoRepository configRepository;
    private final PartidaRepository partidaRepository;
    private final PalpiteRepository palpiteRepository;

    public AdminController(CalculoService calculoService, 
                           UsuarioRepository usuarioRepository, 
                           ConfiguracaoPontuacaoRepository configRepository,
                           PartidaRepository partidaRepository,
                           PalpiteRepository palpiteRepository) {
        this.calculoService = calculoService;
        this.usuarioRepository = usuarioRepository;
        this.configRepository = configRepository;
        this.partidaRepository = partidaRepository;
        this.palpiteRepository = palpiteRepository;
    }

    // --- VALIDAÇÃO DE SEGURANÇA E ESCOPO DE CONTEXTO ---
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

    // --- ROTAS DE CONFIGURAÇÃO DE PONTOS (MATRIZ DINÂMICA DE 5 NÍVEIS) ---
    @GetMapping("/configuracao/{fase}")
    public ResponseEntity<?> getConfiguracaoDaFase(@PathVariable String fase) {
        if (!isUserAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<ConfiguracaoPontuacao> configOpt = configRepository.findByFase(fase);
        if (configOpt.isPresent()) {
            return ResponseEntity.ok(configOpt.get());
        }

        ConfiguracaoPontuacao padrao = new ConfiguracaoPontuacao();
        padrao.setFase(fase);
        return ResponseEntity.ok(padrao);
    }

    @PostMapping("/configuracao")
    public ResponseEntity<?> salvarConfiguracao(@RequestBody ConfiguracaoPontuacao request) {
        System.out.println(">>> TENTATIVA DE SALVAR REGRAS RECEBIDA PARA A FASE: " + request.getFase());

        try {
            if (!isUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"erro\": \"Falha de permissão.\"}");
            }

            if (request.getFase() == null || request.getFase().isEmpty()) {
                System.out.println("❌ ERRO: A fase enviada pelo Front-end está vazia.");
                return ResponseEntity.badRequest().body("{\"erro\": \"Fase não informada\"}");
            }

            Optional<ConfiguracaoPontuacao> existenteOpt = configRepository.findByFase(request.getFase());
            ConfiguracaoPontuacao configFinal;

            if (existenteOpt.isPresent()) {
                configFinal = existenteOpt.get();
                configFinal.setPontosEmpateExato(request.getPontosEmpateExato());
                configFinal.setPontosVencedorExato(request.getPontosVencedorExato());
                configFinal.setPontosVencedorMaisUmGolo(request.getPontosVencedorMaisUmGolo());
                configFinal.setPontosVencedor(request.getPontosVencedor());
                configFinal.setPontosGoloPerdedor(request.getPontosGoloPerdedor());
            } else {
                configFinal = request;
            }

            configRepository.save(configFinal);
            System.out.println("✅ REGRAS SALVAS NO BANCO COM SUCESSO!");
            return ResponseEntity.ok("{\"mensagem\": \"Regras da fase atualizadas com sucesso!\"}");

        } catch (Exception e) {
            System.out.println("🚨 ERRO CRÍTICO AO SALVAR NO BANCO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"erro\": \"Erro interno: " + e.getMessage() + "\"}");
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

    // --- ROTA DE ESTORNO DE PARTIDA (Válvula de Segurança Transacional ACID) ---
    @PostMapping("/partidas/{id}/estornar")
    @Transactional // CRÍTICO: Garante atomicidade total no rollback em cascata dos saldos
    public ResponseEntity<?> estornarPartida(@PathVariable Long id) {
        if (!isUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Acesso negado."));
        }

        Partida partida = partidaRepository.findById(id).orElse(null);
        if (partida == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Partida não encontrada."));
        }

        if (!partida.isFinalizada()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Esta partida não está encerrada."));
        }

        try {
            // Busca TODOS os palpites feitos para este jogo específico
            List<Palpite> palpitesDoJogo = palpiteRepository.findByPartidaId(id);

            // Estorna os pontos da "carteira" de cada utilizador de forma isolada
            for (Palpite palpite : palpitesDoJogo) {
                Usuario usuario = palpite.getUsuario();
                
                // Subtrai apenas o montante exato que o utilizador ganhou neste palpite específico
                int novaPontuacao = usuario.getPontos() - palpite.getPontosGanhos();
                
                // Proteção de borda para evitar inconsistência de pontuação negativa no banco
                if (novaPontuacao < 0) novaPontuacao = 0; 
                
                usuario.setPontos(novaPontuacao);
                
                // Zera o ganho do palpite para limpar o histórico do cartão
                palpite.setPontosGanhos(0);
                
                usuarioRepository.save(usuario);
                palpiteRepository.save(palpite);
            }

            // Limpa os dados de placar oficial e reabre o status para edição no admin
            partida.setGolosEquipaA(null);
            partida.setGolosEquipaB(null);
            partida.setFinalizada(false);
            partidaRepository.save(partida);

            return ResponseEntity.ok(Map.of("mensagem", "Estorno realizado com sucesso. Partida reaberta!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro crítico ao executar estorno: " + e.getMessage()));
        }
    }
}