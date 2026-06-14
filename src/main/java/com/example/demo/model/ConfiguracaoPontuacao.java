package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracao_pontuacao")
public class ConfiguracaoPontuacao {

    @Id
    private String fase = "GLOBAL"; // Usaremos a fase como chave

    private int pontosEmpateExato = 9;
    private int pontosVencedorExato = 7;
    private int pontosVencedorMaisUmGolo = 5;
    private int pontosVencedor = 3;
    private int pontosGoloPerdedor = 1;

    // --- GETTERS E SETTERS ---
    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public int getPontosEmpateExato() { return pontosEmpateExato; }
    public void setPontosEmpateExato(int pontosEmpateExato) { this.pontosEmpateExato = pontosEmpateExato; }

    public int getPontosVencedorExato() { return pontosVencedorExato; }
    public void setPontosVencedorExato(int pontosVencedorExato) { this.pontosVencedorExato = pontosVencedorExato; }

    public int getPontosVencedorMaisUmGolo() { return pontosVencedorMaisUmGolo; }
    public void setPontosVencedorMaisUmGolo(int pontosVencedorMaisUmGolo) { this.pontosVencedorMaisUmGolo = pontosVencedorMaisUmGolo; }

    public int getPontosVencedor() { return pontosVencedor; }
    public void setPontosVencedor(int pontosVencedor) { this.pontosVencedor = pontosVencedor; }

    public int getPontosGoloPerdedor() { return pontosGoloPerdedor; }
    public void setPontosGoloPerdedor(int pontosGoloPerdedor) { this.pontosGoloPerdedor = pontosGoloPerdedor; }
}