package com.example.demo.controller;

import com.example.demo.model.Partida;
import com.example.demo.repository.PartidaRepository;
import com.example.demo.service.CalculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/partidas")
public class PartidaController {

    private final PartidaRepository partidaRepository;
    private final CalculoService calculoService;

    // Injeção de dependências
    public PartidaController(PartidaRepository partidaRepository, CalculoService calculoService) {
        this.partidaRepository = partidaRepository;
        this.calculoService = calculoService;
    }

    @GetMapping
    public ResponseEntity<List<Partida>> listarPartidas() {
        return ResponseEntity.ok(partidaRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Partida> criarPartida(@RequestBody Partida partida) {
        partida.setFinalizada(false);
        return ResponseEntity.status(HttpStatus.CREATED).body(partidaRepository.save(partida));
    }

    // Endpoint seguro para o Admin
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarPartida(@PathVariable Long id, @RequestBody Partida placarFinal) {
        
        Optional<Partida> partidaOpt = partidaRepository.findById(id);

        if (partidaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Partida não encontrada\"}");
        }

        Partida partida = partidaOpt.get();

        if (partida.isFinalizada()) {
            return ResponseEntity.badRequest().body("{\"erro\": \"Esta partida já foi encerrada e os pontos processados.\"}");
        }

        // Atualiza os dados no banco
        partida.setGolosEquipaA(placarFinal.getGolosEquipaA());
        partida.setGolosEquipaB(placarFinal.getGolosEquipaB());
        partida.setFinalizada(true);
        Partida partidaAtualizada = partidaRepository.save(partida);

        // Aciona o motor de regras
        calculoService.calcularPontos(partidaAtualizada);

        return ResponseEntity.ok(partidaAtualizada);
    }
}