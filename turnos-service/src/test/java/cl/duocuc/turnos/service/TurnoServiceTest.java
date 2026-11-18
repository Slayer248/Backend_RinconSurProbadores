package cl.duocuc.turnos.service;

import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private TurnoService turnoService;

    @Test
    void crearTurno_ExitosoGeneraCorrelativoEnEspera() {
        when(turnoRepository.existsByNombreClienteIgnoreCaseAndEstado("Camila", "EN_ESPERA")).thenReturn(false);
        when(turnoRepository.count()).thenReturn(0L);
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        Turno turno = turnoService.crear(new CrearTurnoRequest("Camila", 4, true));

        assertEquals("T-1001", turno.getNumeroTurno());
        assertEquals("EN_ESPERA", turno.getEstado());
        assertTrue(turno.getEsPreferente());
    }

    @Test
    void crearTurno_PrendasFueraDeRangoLanzaBadRequest() {
        CrearTurnoRequest req = new CrearTurnoRequest("Carlos", 8, false);

        assertThrows(ResponseStatusException.class, () -> turnoService.crear(req));
        verify(turnoRepository, never()).save(any());
    }
}