package com.example.demo.config;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.model.Partida;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import com.example.demo.repository.PartidaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final List<String> FASES_CANONICAS = List.of(
            "Fase de Grupos",
            "16 Avos de Final",
            "Oitavas de Final",
            "Quartas de Final",
            "Semifinais",
            "Final"
    );

    @Bean
    public CommandLineRunner carregarRegrasPadrao(ConfiguracaoPontuacaoRepository configRepository) {
        return args -> {
            for (String fase : FASES_CANONICAS) {
                if (configRepository.findByFase(fase).isEmpty()) {
                    ConfiguracaoPontuacao config = new ConfiguracaoPontuacao();
                    config.setFase(fase);
                    configRepository.save(config);
                }
            }
        };
    }

    @Bean
    public CommandLineRunner carregarJogosDaCopa(PartidaRepository partidaRepository) {
        return args -> {
            if (partidaRepository.count() == 0) {
                
                partidaRepository.saveAll(Arrays.asList(
                    // === GRUPO A ===
                    new Partida("Grupo A - 1ª Rodada", "México", "África do Sul", LocalDateTime.of(2026, 6, 11, 16, 0)),
                    new Partida("Grupo A - 1ª Rodada", "Coreia do Sul", "República Tcheca", LocalDateTime.of(2026, 6, 11, 23, 0)),
                    new Partida("Grupo A - 2ª Rodada", "República Tcheca", "África do Sul", LocalDateTime.of(2026, 6, 18, 13, 0)),
                    new Partida("Grupo A - 2ª Rodada", "México", "Coreia do Sul", LocalDateTime.of(2026, 6, 18, 22, 0)),
                    new Partida("Grupo A - 3ª Rodada", "República Tcheca", "México", LocalDateTime.of(2026, 6, 24, 17, 0)),
                    new Partida("Grupo A - 3ª Rodada", "África do Sul", "Coreia do Sul", LocalDateTime.of(2026, 6, 24, 17, 0)),

                    // === GRUPO B ===
                    new Partida("Grupo B - 1ª Rodada", "Canadá", "Bósnia e Herzegovina", LocalDateTime.of(2026, 6, 12, 16, 0)),
                    new Partida("Grupo B - 1ª Rodada", "Catar", "Suíça", LocalDateTime.of(2026, 6, 13, 16, 0)),
                    new Partida("Grupo B - 2ª Rodada", "Suíça", "Bósnia e Herzegovina", LocalDateTime.of(2026, 6, 18, 19, 0)),
                    new Partida("Grupo B - 2ª Rodada", "Canadá", "Catar", LocalDateTime.of(2026, 6, 18, 22, 0)),
                    new Partida("Grupo B - 3ª Rodada", "Suíça", "Canadá", LocalDateTime.of(2026, 6, 24, 16, 0)),
                    new Partida("Grupo B - 3ª Rodada", "Bósnia e Herzegovina", "Catar", LocalDateTime.of(2026, 6, 24, 16, 0)),

                    // === GRUPO C ===
                    new Partida("Grupo C - 1ª Rodada", "Brasil", "Marrocos", LocalDateTime.of(2026, 6, 13, 19, 0)),
                    new Partida("Grupo C - 1ª Rodada", "Haiti", "Escócia", LocalDateTime.of(2026, 6, 13, 22, 0)),
                    new Partida("Grupo C - 2ª Rodada", "Escócia", "Marrocos", LocalDateTime.of(2026, 6, 19, 16, 0)),
                    new Partida("Grupo C - 2ª Rodada", "Brasil", "Haiti", LocalDateTime.of(2026, 6, 19, 21, 30)),
                    new Partida("Grupo C - 3ª Rodada", "Escócia", "Brasil", LocalDateTime.of(2026, 6, 24, 19, 0)),
                    new Partida("Grupo C - 3ª Rodada", "Marrocos", "Haiti", LocalDateTime.of(2026, 6, 24, 19, 0)),

                    // === GRUPO D ===
                    new Partida("Grupo D - 1ª Rodada", "EUA", "Paraguai", LocalDateTime.of(2026, 6, 12, 22, 0)),
                    new Partida("Grupo D - 1ª Rodada", "Austrália", "Turquia", LocalDateTime.of(2026, 6, 14, 1, 0)),
                    new Partida("Grupo D - 2ª Rodada", "Turquia", "Paraguai", LocalDateTime.of(2026, 6, 19, 19, 0)),
                    new Partida("Grupo D - 2ª Rodada", "EUA", "Austrália", LocalDateTime.of(2026, 6, 19, 22, 0)),
                    new Partida("Grupo D - 3ª Rodada", "Turquia", "EUA", LocalDateTime.of(2026, 6, 25, 18, 0)),
                    new Partida("Grupo D - 3ª Rodada", "Paraguai", "Austrália", LocalDateTime.of(2026, 6, 25, 18, 0)),

                    // === GRUPO E ===
                    new Partida("Grupo E - 1ª Rodada", "Alemanha", "Curaçao", LocalDateTime.of(2026, 6, 14, 14, 0)),
                    new Partida("Grupo E - 1ª Rodada", "Costa do Marfim", "Equador", LocalDateTime.of(2026, 6, 14, 20, 0)),
                    new Partida("Grupo E - 2ª Rodada", "Equador", "Curaçao", LocalDateTime.of(2026, 6, 20, 17, 0)),
                    new Partida("Grupo E - 2ª Rodada", "Alemanha", "Costa do Marfim", LocalDateTime.of(2026, 6, 20, 20, 0)),
                    new Partida("Grupo E - 3ª Rodada", "Equador", "Alemanha", LocalDateTime.of(2026, 6, 25, 17, 0)),
                    new Partida("Grupo E - 3ª Rodada", "Curaçao", "Costa do Marfim", LocalDateTime.of(2026, 6, 25, 17, 0)),

                    // === GRUPO F ===
                    new Partida("Grupo F - 1ª Rodada", "Holanda", "Japão", LocalDateTime.of(2026, 6, 14, 17, 0)),
                    new Partida("Grupo F - 1ª Rodada", "Suécia", "Tunísia", LocalDateTime.of(2026, 6, 14, 23, 0)),
                    new Partida("Grupo F - 2ª Rodada", "Holanda", "Suécia", LocalDateTime.of(2026, 6, 20, 14, 0)),
                    new Partida("Grupo F - 2ª Rodada", "Tunísia", "Japão", LocalDateTime.of(2026, 6, 20, 20, 0)),
                    new Partida("Grupo F - 3ª Rodada", "Japão", "Suécia", LocalDateTime.of(2026, 6, 26, 16, 0)),
                    new Partida("Grupo F - 3ª Rodada", "Tunísia", "Holanda", LocalDateTime.of(2026, 6, 26, 16, 0)),

                    // === GRUPO G ===
                    new Partida("Grupo G - 1ª Rodada", "Bélgica", "Egito", LocalDateTime.of(2026, 6, 15, 16, 0)),
                    new Partida("Grupo G - 1ª Rodada", "Irã", "Nova Zelândia", LocalDateTime.of(2026, 6, 15, 22, 0)),
                    new Partida("Grupo G - 2ª Rodada", "Bélgica", "Irã", LocalDateTime.of(2026, 6, 21, 16, 0)),
                    new Partida("Grupo G - 2ª Rodada", "Nova Zelândia", "Egito", LocalDateTime.of(2026, 6, 21, 22, 0)),
                    new Partida("Grupo G - 3ª Rodada", "Egito", "Irã", LocalDateTime.of(2026, 6, 26, 20, 0)),
                    new Partida("Grupo G - 3ª Rodada", "Nova Zelândia", "Bélgica", LocalDateTime.of(2026, 6, 26, 20, 0)),

                    // === GRUPO H ===
                    new Partida("Grupo H - 1ª Rodada", "Espanha", "Cabo Verde", LocalDateTime.of(2026, 6, 15, 13, 0)),
                    new Partida("Grupo H - 1ª Rodada", "Arábia Saudita", "Uruguai", LocalDateTime.of(2026, 6, 15, 19, 0)),
                    new Partida("Grupo H - 2ª Rodada", "Espanha", "Arábia Saudita", LocalDateTime.of(2026, 6, 21, 13, 0)),
                    new Partida("Grupo H - 2ª Rodada", "Uruguai", "Cabo Verde", LocalDateTime.of(2026, 6, 21, 19, 0)),
                    new Partida("Grupo H - 3ª Rodada", "Cabo Verde", "Arábia Saudita", LocalDateTime.of(2026, 6, 26, 21, 0)),
                    new Partida("Grupo H - 3ª Rodada", "Uruguai", "Espanha", LocalDateTime.of(2026, 6, 26, 21, 0)),

                    // === GRUPO I ===
                    new Partida("Grupo I - 1ª Rodada", "França", "Senegal", LocalDateTime.of(2026, 6, 16, 16, 0)),
                    new Partida("Grupo I - 1ª Rodada", "Iraque", "Noruega", LocalDateTime.of(2026, 6, 16, 19, 0)),
                    new Partida("Grupo I - 2ª Rodada", "França", "Iraque", LocalDateTime.of(2026, 6, 22, 14, 0)),
                    new Partida("Grupo I - 2ª Rodada", "Noruega", "Senegal", LocalDateTime.of(2026, 6, 22, 18, 0)),
                    new Partida("Grupo I - 3ª Rodada", "Noruega", "França", LocalDateTime.of(2026, 6, 26, 16, 0)),
                    new Partida("Grupo I - 3ª Rodada", "Senegal", "Iraque", LocalDateTime.of(2026, 6, 26, 16, 0)),

                    // === GRUPO J ===
                    new Partida("Grupo J - 1ª Rodada", "Argentina", "Argélia", LocalDateTime.of(2026, 6, 16, 22, 0)),
                    new Partida("Grupo J - 1ª Rodada", "Áustria", "Jordânia", LocalDateTime.of(2026, 6, 17, 1, 0)),
                    new Partida("Grupo J - 2ª Rodada", "Jordânia", "Argélia", LocalDateTime.of(2026, 6, 22, 21, 0)),
                    new Partida("Grupo J - 2ª Rodada", "Argentina", "Áustria", LocalDateTime.of(2026, 6, 22, 23, 0)),
                    new Partida("Grupo J - 3ª Rodada", "Argélia", "Áustria", LocalDateTime.of(2026, 6, 27, 23, 0)),
                    new Partida("Grupo J - 3ª Rodada", "Jordânia", "Argentina", LocalDateTime.of(2026, 6, 27, 23, 0)),

                    // === GRUPO K ===
                    new Partida("Grupo K - 1ª Rodada", "Portugal", "Congo DR", LocalDateTime.of(2026, 6, 17, 14, 0)),
                    new Partida("Grupo K - 1ª Rodada", "Uzbequistão", "Colômbia", LocalDateTime.of(2026, 6, 17, 23, 0)),
                    new Partida("Grupo K - 2ª Rodada", "Colômbia", "Congo DR", LocalDateTime.of(2026, 6, 23, 14, 0)),
                    new Partida("Grupo K - 2ª Rodada", "Portugal", "Uzbequistão", LocalDateTime.of(2026, 6, 23, 20, 0)),
                    new Partida("Grupo K - 3ª Rodada", "Colômbia", "Portugal", LocalDateTime.of(2026, 6, 27, 20, 30)),
                    new Partida("Grupo K - 3ª Rodada", "Congo DR", "Uzbequistão", LocalDateTime.of(2026, 6, 27, 20, 30)),

                    // === GRUPO L ===
                    new Partida("Grupo L - 1ª Rodada", "Inglaterra", "Croácia", LocalDateTime.of(2026, 6, 17, 17, 0)),
                    new Partida("Grupo L - 1ª Rodada", "Gana", "Panamá", LocalDateTime.of(2026, 6, 17, 20, 0)),
                    new Partida("Grupo L - 2ª Rodada", "Inglaterra", "Gana", LocalDateTime.of(2026, 6, 23, 17, 0)),
                    new Partida("Grupo L - 2ª Rodada", "Panamá", "Croácia", LocalDateTime.of(2026, 6, 23, 20, 0)),
                    new Partida("Grupo L - 3ª Rodada", "Panamá", "Inglaterra", LocalDateTime.of(2026, 6, 27, 18, 0)),
                    new Partida("Grupo L - 3ª Rodada", "Croácia", "Gana", LocalDateTime.of(2026, 6, 27, 18, 0))
                ));
                
                System.out.println("✅ DADOS DA COPA DO MUNDO DE 2026 INJETADOS COM AS DATAS EXATAS (BRT)!");
            }
        };
    }
}