package com.example.demo.controller;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ConfiguracaoPontuacao salvarRegra(@RequestBody ConfiguracaoPontuacao config) {
        return repository.save(config);
    }
}