package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracao_pontuacao")
public class ConfiguracaoPontuacao {

    @Id
    private String fase = "GLOBAL"; // Usaremos 'GLOBAL' para aplicar a todos os jogos por padrão

    private int pontosPlacarExato = 25;
    private int pontosVencedor = 10;
    private int pontosGoloEquipa = 5; // A TUA NOVA REGRA

    // Getters e Setters
    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public int getPontosPlacarExato() { return pontosPlacarExato; }
    public void setPontosPlacarExato(int pontosPlacarExato) { this.pontosPlacarExato = pontosPlacarExato; }

    public int getPontosVencedor() { return pontosVencedor; }
    public void setPontosVencedor(int pontosVencedor) { this.pontosVencedor = pontosVencedor; }

    public int getPontosGoloEquipa() { return pontosGoloEquipa; }
    public void setPontosGoloEquipa(int pontosGoloEquipa) { this.pontosGoloEquipa = pontosGoloEquipa; }
}