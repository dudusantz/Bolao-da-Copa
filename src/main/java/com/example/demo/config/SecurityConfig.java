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
                // Libera os ficheiros visuais e as pastas de assets
                .requestMatchers("/", "/index.html", "/cadastro.html", "/login.html", "/recuperar-senha.html", "/redefinir-senha.html", "/api/auth/**").permitAll()
                .requestMatchers("/css/**", "/img/**").permitAll()
                
                // Libera os endpoints REST de autenticação pública
                .requestMatchers(HttpMethod.POST, "/usuarios/registrar", "/api/auth/login").permitAll()

                // Regras de ADMIN
                .requestMatchers(HttpMethod.POST, "/partidas", "/configuracoes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/partidas/**").hasRole("ADMIN")
                
                // Regras de utilizadores autenticados
                .requestMatchers(HttpMethod.GET, "/partidas", "/usuarios/ranking").authenticated()
                .requestMatchers(HttpMethod.POST, "/palpites").authenticated()
                
                .anyRequest().authenticated()
            );
            // .httpBasic() removido propositadamente para não conflitar com a Fetch API

        return http.build();
    }

    // 1. Define o BCrypt como o padrão de criptografia
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Expõe o AuthenticationManager (necessário para o nosso Controller de Login REST)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}