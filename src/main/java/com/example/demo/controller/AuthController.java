package com.example.demo.controller;

import com.example.demo.model.TokenRecuperacao;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TokenRecuperacaoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRecuperacaoRepository tokenRepository;
    private final EmailService emailService;

    // Construtor com todas as dependências injetadas
    public AuthController(AuthenticationManager authenticationManager, 
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          TokenRecuperacaoRepository tokenRepository,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    // --- 1. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        System.out.println(">>> TENTATIVA DE LOGIN RECEBIDA: " + loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(context, request, response);

            System.out.println(">>> LOGIN AUTORIZADO E SESSÃO GUARDADA!");
            return ResponseEntity.ok().body("{\"mensagem\": \"Login efetuado com sucesso\"}");
            
        } catch (Exception e) {
            System.out.println(">>> FALHA NO LOGIN: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Credenciais inválidas\"}");
        }
    }

    // --- 2. REGISTRO (Movido para cá para alinhar com o Front-end) ---
    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(@RequestBody Usuario usuario) {
        if (usuario.getNome() == null || usuario.getEmail() == null || usuario.getSenha() == null) {
            return ResponseEntity.badRequest().body("{\"erro\": \"Todos os campos são obrigatórios!\"}");
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("{\"erro\": \"Este e-mail já está registado!\"}");
        }

        usuario.setPontos(0);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("{\"mensagem\": \"Utilizador registado com sucesso!\"}");
    }

    // --- 3. DADOS DA SESSÃO ---
    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Não autenticado\"}");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(userDetails.getUsername());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // CORREÇÃO CRÍTICA: Adicionado o campo "admin" no JSON para o botão do Front-end funcionar
            return ResponseEntity.ok(String.format(
                "{\"id\": %d, \"nome\": \"%s\", \"email\": \"%s\", \"pontos\": %d, \"admin\": %b}", 
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPontos(), usuario.isAdmin()
            ));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Utilizador não encontrado no banco de dados\"}");
    }

    // --- 4. RECUPERAÇÃO DE SENHA ---
    @PostMapping("/recuperar-senha")
    @Transactional
    public ResponseEntity<?> solicitarRecuperacao(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.ok("{\"mensagem\": \"Se o e-mail estiver cadastrado, um link de redefinição foi enviado.\"}");
        }

        Usuario usuario = usuarioOpt.get();
        tokenRepository.deleteByUsuario(usuario);

        TokenRecuperacao tokenObj = new TokenRecuperacao(usuario);
        tokenRepository.save(tokenObj);

        // URL para testes locais. No futuro, alterar para o domínio real.
        String linkReal = "http://localhost:8080/redefinir-senha.html?token=" + tokenObj.getToken();
        
        emailService.enviarEmailRecuperacao(usuario.getEmail(), usuario.getNome(), linkReal);

        return ResponseEntity.ok("{\"mensagem\": \"Se o e-mail estiver cadastrado, um link de redefinição foi enviado.\"}");
    }

    // --- 5. REDEFINIÇÃO DE SENHA ---
    @PostMapping("/redefinir-senha")
    @Transactional
    public ResponseEntity<?> executarRedefinicao(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String novaSenha = request.get("senha");

        Optional<TokenRecuperacao> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpirado()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"erro\": \"Link inválido ou expirado. Solicite uma nova redefinição.\"}");
        }

        TokenRecuperacao tokenValido = tokenOpt.get();
        Usuario usuario = tokenValido.getUsuario();

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        tokenRepository.delete(tokenValido);

        return ResponseEntity.ok("{\"mensagem\": \"Senha alterada com sucesso!\"}");
    }
}

// DTO Auxiliar
class LoginDTO {
    private String email;
    private String senha;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}