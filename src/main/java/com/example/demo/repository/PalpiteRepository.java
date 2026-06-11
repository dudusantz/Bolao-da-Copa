package com.example.demo.repository;

import com.example.demo.model.Palpite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalpiteRepository extends JpaRepository<Palpite, Long> {
    
    // Busca todos os palpites feitos por um utilizador específico (Usado no Front-end para a aba "Meus Resultados")
    List<Palpite> findByUsuarioId(Long usuarioId);

    // Busca todos os palpites de um jogo específico (Usado pelo CalculoService)
    List<Palpite> findByPartidaId(Long partidaId);
    
    // CORREÇÃO: Usar List em vez de Optional evita o crash 'NonUniqueResultException'
    List<Palpite> findByUsuarioIdAndPartidaId(Long usuarioId, Long partidaId);
}