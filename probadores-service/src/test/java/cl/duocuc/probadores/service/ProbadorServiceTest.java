package cl.duocuc.probadores.service;

import cl.duocuc.probadores.client.TurnoClient;
import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProbadorServiceTest {

    @Mock
    private ProbadorRepository probadorRepository;

    @Mock
    private TurnoClient turnoClient;

    @InjectMocks
    private ProbadorService probadorService;

    @Test
    void crearProbador_ExitosoIniciaLibre() {
        when(probadorRepository.existsById("P-04")).thenReturn(false);
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador creado = probadorService.crear(new CrearProbadorRequest("p-04", true));

        assertEquals("P-04", creado.getCodigo());
        assertEquals("LIBRE", creado.getEstado());
        assertTrue(creado.getEsAccesible());
    }

    @Test
    void cambiarEstadoAOcupado_AtiendeSiguienteTurnoEnCola() {
        Probador libre = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").desdeHora("-").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(libre));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador ocupado = probadorService.cambiarEstado("P-01", "OCUPADO");

        assertEquals("OCUPADO", ocupado.getEstado());
        assertNotEquals("-", ocupado.getDesdeHora());
        verify(turnoClient, times(1)).atenderSiguienteSiExiste();
    }

    @Test
    void bloquearProbador_OcupadoLanzaConflicto() {
        Probador ocupado = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").desdeHora("14:30").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(ocupado));

        assertThrows(ResponseStatusException.class, () ->
                probadorService.cambiarBloqueo("P-01", new BloqueoProbadorRequest(true, "Mantención"))
        );
        verify(probadorRepository, never()).save(any());
    }
}