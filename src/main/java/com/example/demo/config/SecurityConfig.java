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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                
                // 1. A MÁGICA: Liberta TODOS os ficheiros de interface de uma só vez
                .requestMatchers("/", "/*.html", "/*.css", "/css/**", "/img/**").permitAll()
                
                // 2. Liberta TODO o tráfego de autenticação (Login, Registo, Recuperar Senha)
                .requestMatchers("/api/auth/**").permitAll()

                // 3. Regras de ADMIN (Protege as rotas da API)
                // Nota: O teu AdminController já tem a trava isUserAdmin() internamente
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/partidas", "/configuracoes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/partidas/**").hasRole("ADMIN")
                
                // 4. Regras de Utilizadores Autenticados (Ações comuns do jogo)
                .requestMatchers(HttpMethod.GET, "/partidas", "/usuarios/ranking").authenticated()
                .requestMatchers(HttpMethod.POST, "/palpites").authenticated()
                
                // 5. Qualquer outra rota não mapeada fica trancada por padrão
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