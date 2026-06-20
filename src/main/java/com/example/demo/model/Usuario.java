package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private Integer pontos;
    
    // NOMECLATURA CORRETA: A variável chama-se 'admin', a coluna no Supabase é 'is_admin'
    @Column(name = "is_admin", nullable = false, columnDefinition = "boolean default false")
    private boolean admin = false;

    // --- VARIÁVEIS TRANSIENTES PARA O MOTOR DE DESEMPATE ---
    // O @Transient avisa o Hibernate: "Não tentes guardar isto na base de dados".
    // Servem apenas para transportar os cálculos em memória para o Front-end.
    @Transient
    private int acertosVencedorExato;

    @Transient
    private int acertosEmpateExato;

    @Transient
    private int acertosVencedorMaisGolo;

    @Transient
    private int acertosVencedor;

    @Transient
    private int acertosUmPlacar;

    // Construtor vazio obrigatório para o Hibernate
    public Usuario() {}

    // Construtor para facilitar o registo
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.pontos = 0; // Todo utilizador começa com 0 pontos
        this.admin = false; // Garante que ninguém nasce administrador por acidente
    }

    // --- Getters e Setters Base ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; } 
    public void setSenha(String senha) { this.senha = senha; } 

    public Integer getPontos() { return pontos; }
    public void setPontos(Integer pontos) { this.pontos = pontos; }

    // O getter de boolean no Java DEVE começar por 'is'
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }

    // --- Getters e Setters do Desempate ---
    public int getAcertosVencedorExato() { return acertosVencedorExato; }
    public void setAcertosVencedorExato(int acertosVencedorExato) { this.acertosVencedorExato = acertosVencedorExato; }

    public int getAcertosEmpateExato() { return acertosEmpateExato; }
    public void setAcertosEmpateExato(int acertosEmpateExato) { this.acertosEmpateExato = acertosEmpateExato; }

    public int getAcertosVencedorMaisGolo() { return acertosVencedorMaisGolo; }
    public void setAcertosVencedorMaisGolo(int acertosVencedorMaisGolo) { this.acertosVencedorMaisGolo = acertosVencedorMaisGolo; }

    public int getAcertosVencedor() { return acertosVencedor; }
    public void setAcertosVencedor(int acertosVencedor) { this.acertosVencedor = acertosVencedor; }

    public int getAcertosUmPlacar() { return acertosUmPlacar; }
    public void setAcertosUmPlacar(int acertosUmPlacar) { this.acertosUmPlacar = acertosUmPlacar; }
}