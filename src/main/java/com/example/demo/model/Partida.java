package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fase;
    private String equipaA;
    private String equipaB;
    
    // Inicializamos com 0 para evitar NullPointerException antes de o Admin colocar o placar real
    private Integer golosEquipaA = 0;
    private Integer golosEquipaB = 0;
    
    private LocalDateTime dataHoraDoJogo;
    private boolean finalizada = false;

    // 1. CONSTRUTOR OBRIGATÓRIO PARA O HIBERNATE/SPRING DATA
    public Partida() {}

    // 2. CONSTRUTOR QUE RESOLVE O TEU ERRO NO DATA INITIALIZER
    public Partida(String fase, String equipaA, String equipaB, LocalDateTime dataHoraDoJogo) {
        this.fase = fase;
        this.equipaA = equipaA;
        this.equipaB = equipaB;
        this.dataHoraDoJogo = dataHoraDoJogo;
        this.golosEquipaA = 0;
        this.golosEquipaB = 0;
        this.finalizada = false;
    }

    // --- GETTERS E SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public String getEquipaA() {
        return equipaA;
    }

    public void setEquipaA(String equipaA) {
        this.equipaA = equipaA;
    }

    public String getEquipaB() {
        return equipaB;
    }

    public void setEquipaB(String equipaB) {
        this.equipaB = equipaB;
    }

    public Integer getGolosEquipaA() {
        return golosEquipaA;
    }

    public void setGolosEquipaA(Integer golosEquipaA) {
        this.golosEquipaA = golosEquipaA;
    }

    public Integer getGolosEquipaB() {
        return golosEquipaB;
    }

    public void setGolosEquipaB(Integer golosEquipaB) {
        this.golosEquipaB = golosEquipaB;
    }

    public LocalDateTime getDataHoraDoJogo() {
        return dataHoraDoJogo;
    }

    public void setDataHoraDoJogo(LocalDateTime dataHoraDoJogo) {
        this.dataHoraDoJogo = dataHoraDoJogo;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }
}