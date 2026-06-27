package com.example.demo.controller;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/configuracoes")
public class ConfiguracaoPontuacaoController {

    @Autowired
    private ConfiguracaoPontuacaoRepository repository;

    @GetMapping
    public List<ConfiguracaoPontuacao> listarRegras() {
        return repository.findAll();
    }

    @PostMapping
    public ConfiguracaoPontuacao salvarRegra(@RequestBody ConfiguracaoPontuacao request) {
        Optional<ConfiguracaoPontuacao> existenteOpt = repository.findByFase(request.getFase());
        if (existenteOpt.isPresent()) {
            ConfiguracaoPontuacao configFinal = existenteOpt.get();
            configFinal.setPontosEmpateExato(request.getPontosEmpateExato());
            configFinal.setPontosVencedorExato(request.getPontosVencedorExato());
            configFinal.setPontosVencedorMaisUmGolo(request.getPontosVencedorMaisUmGolo());
            configFinal.setPontosVencedor(request.getPontosVencedor());
            configFinal.setPontosGoloPerdedor(request.getPontosGoloPerdedor());
            return repository.save(configFinal);
        }
        return repository.save(request);
    }
}