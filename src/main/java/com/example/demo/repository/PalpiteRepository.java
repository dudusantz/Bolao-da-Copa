package com.example.demo.repository;

import com.example.demo.model.Palpite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PalpiteRepository extends JpaRepository<Palpite, Long> {
    
    // Busca todos os palpites feitos por um utilizador específico (Usado no Front-end para a aba "Meus Resultados")
    List<Palpite> findByUsuarioId(Long usuarioId);

    // Busca todos os palpites de um jogo específico (Usado pelo CalculoService)
    List<Palpite> findByPartidaId(Long partidaId);
    
    // CORREÇÃO: Usar List em vez de Optional evita o crash 'NonUniqueResultException'
    List<Palpite> findByUsuarioIdAndPartidaId(Long usuarioId, Long partidaId);

    // --- NOVA QUERY OTIMIZADA PARA O MODAL DE ESPIONAGEM ---
    // Faz apenas 1 requisição ao banco trazendo Palpites e Partidas juntos e já filtrados pelo relógio
    @Query("SELECT p FROM Palpite p JOIN FETCH p.partida pt WHERE p.usuario.id = :usuarioId AND pt.dataHoraDoJogo <= :agora")
    List<Palpite> findHistoricoVisivelPorUsuario(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);
}