package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "palpites")
public class Palpite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore bloqueia o Loop Infinito. O Front-end não precisa de receber os 
    // dados do utilizador repetidos dentro de cada palpite (ele já sabe quem é).
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore 
    private Usuario usuario;

    // Mantemos a partida visível para o Front-end conseguir renderizar o Dashboard,
    // mas evitamos que a Partida tente carregar os palpites dela em loop.
    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    @JsonIgnoreProperties("palpites") 
    private Partida partida;

    private Integer golosEquipaA;
    private Integer golosEquipaB;

    private Integer pontosGanhos = 0;

    public Palpite() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public Integer getGolosEquipaA() { return golosEquipaA; }
    public void setGolosEquipaA(Integer golosEquipaA) { this.golosEquipaA = golosEquipaA; }

    public Integer getGolosEquipaB() { return golosEquipaB; }
    public void setGolosEquipaB(Integer golosEquipaB) { this.golosEquipaB = golosEquipaB; }

    public Integer getPontosGanhos() { return pontosGanhos; }
    public void setPontosGanhos(Integer pontosGanhos) { this.pontosGanhos = pontosGanhos; }
}