package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            
            // --- CORREÇÃO DO ERRO 403 (SESSÃO EXPIRADA) ---
            // Quando um acesso for negado por falta de sessão, redireciona para o /login em vez de mostrar a tela de erro 403
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            
            // --- CONFIGURAÇÃO DO MANTER CONECTADO (REMEMBER-ME) ---
            .rememberMe(remember -> remember
                .key("bolao-ademicon-secret-key-2026") 
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(2592000) // 30 dias
            )

            .authorizeHttpRequests(auth -> auth
                
                // 1. Liberta os ficheiros físicos E as rotas limpas (Pretty URLs)
                .requestMatchers(
                    "/", 
                    "/login", 
                    "/cadastro", 
                    "/recuperar-senha", 
                    "/redefinir-senha", 
                    "/*.html", 
                    "/*.css", 
                    "/css/**", 
                    "/img/**"
                ).permitAll()
                
                // 2. Liberta TODO o tráfego de autenticação da API
                .requestMatchers("/api/auth/**").permitAll()

                // 3. Regras da Interface Visual Trancada
                .requestMatchers("/admin-panel").authenticated() // Protege a tela do admin
                .requestMatchers("/dashboard").authenticated()   // Protege a tela do utilizador

                // 4. Regras de ADMIN (Protege as rotas de DADOS da API)
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/partidas", "/configuracoes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/partidas/**").hasRole("ADMIN")
                
                // 5. Regras de Utilizadores Autenticados (Ações comuns do jogo)
                .requestMatchers(HttpMethod.GET, "/partidas", "/usuarios/ranking").authenticated()
                .requestMatchers(HttpMethod.POST, "/palpites").authenticated()
                
                // 6. Qualquer outra rota não mapeada fica trancada por padrão
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // Define o BCrypt como o padrão de criptografia
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Expõe o AuthenticationManager para o AuthController funcionar
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}