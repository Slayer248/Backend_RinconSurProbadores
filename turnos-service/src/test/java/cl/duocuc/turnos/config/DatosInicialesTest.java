package cl.duocuc.turnos.config;

import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Captor
    private ArgumentCaptor<List<Turno>> turnos;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_FilaVacia_InsertaClientesConCorrelativoContinuo() {
        when(turnoRepository.existsByEstado("EN_ESPERA")).thenReturn(false);
        when(turnoRepository.count()).thenReturn(10L);

        datosIniciales.run();

        verify(turnoRepository).saveAll(turnos.capture());
        assertEquals(4, turnos.getValue().size());
        assertEquals("T-1011", turnos.getValue().get(0).getNumeroTurno());
    }

    @Test
    void run_FilaConClientes_NoInserta() {
        when(turnoRepository.existsByEstado("EN_ESPERA")).thenReturn(true);

        datosIniciales.run();

        verify(turnoRepository, never()).saveAll(anyList());
    }
}
