package com.example.demo.repository;

import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // MÁGICA DO SPRING: Procura um utilizador pelo e-mail exato para o Login
    // Usamos Optional para evitar NullPointerExceptions caso o e-mail não exista
    Optional<Usuario> findByEmail(String email);
    
}