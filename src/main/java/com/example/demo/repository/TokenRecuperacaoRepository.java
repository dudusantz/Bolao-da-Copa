package com.example.demo.repository;

import com.example.demo.model.TokenRecuperacao;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Long> {
    Optional<TokenRecuperacao> findByToken(String token);
    void deleteByUsuario(Usuario usuario); // Limpa tokens antigos ao gerar um novo
}