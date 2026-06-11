package com.example.demo.repository;

import com.example.demo.model.ConfiguracaoPontuacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoPontuacaoRepository extends JpaRepository<ConfiguracaoPontuacao, String> {
}