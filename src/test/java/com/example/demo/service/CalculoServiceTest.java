package com.example.demo.service;

import com.example.demo.model.ConfiguracaoPontuacao;
import com.example.demo.model.Partida;
import com.example.demo.model.Palpite;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ConfiguracaoPontuacaoRepository;
import com.example.demo.repository.PalpiteRepository;
import com.example.demo.repository.PartidaRepository;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculoServiceTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private PalpiteRepository palpiteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ConfiguracaoPontuacaoRepository configRepository;

    @InjectMocks private CalculoService calculoService;

    @Test
    void deveBuscarRegrasPelaFaseCanonicaDaPartida() {
        Partida partida = new Partida();
        partida.setId(1L);
        partida.setFase("Grupo C - 2ª Rodada");
        partida.setGolosEquipaA(2);
        partida.setGolosEquipaB(1);

        ConfiguracaoPontuacao regra = new ConfiguracaoPontuacao();
        regra.setFase("Fase de Grupos");

        Palpite palpite = new Palpite();
        palpite.setGolosEquipaA(2);
        palpite.setGolosEquipaB(1);
        Usuario usuario = new Usuario();
        usuario.setPontos(0);
        palpite.setUsuario(usuario);

        when(palpiteRepository.findByPartidaId(1L)).thenReturn(List.of(palpite));
        when(configRepository.findByFase("Fase de Grupos")).thenReturn(Optional.of(regra));

        calculoService.calcularPontos(partida);

        verify(configRepository).findByFase("Fase de Grupos");
    }

    @Test
    void deveFalharQuandoRegraDaFaseNaoExistir() {
        Partida partida = new Partida();
        partida.setId(2L);
        partida.setFase("16 Avos de Final");
        partida.setGolosEquipaA(1);
        partida.setGolosEquipaB(0);

        when(palpiteRepository.findByPartidaId(2L)).thenReturn(List.of(new Palpite()));
        when(configRepository.findByFase("16 Avos de Final")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> calculoService.calcularPontos(partida));
    }
}
