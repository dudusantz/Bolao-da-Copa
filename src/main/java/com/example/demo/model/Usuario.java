package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    // --- Getters e Setters ---
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
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}