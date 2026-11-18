package cl.duocuc.turnos.controller;

import cl.duocuc.turnos.dto.CambiarEstadoTurnoRequest;
import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.service.TurnoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController turnoController;

    @Test
    void listar_DelegaAlServicio() {
        when(turnoService.listarPorEstado("EN_ESPERA"))
                .thenReturn(List.of(Turno.builder().id(1L).numeroTurno("T-1001").build()));

        List<Turno> res = turnoController.listar("EN_ESPERA");

        assertEquals(1, res.size());
        verify(turnoService).listarPorEstado("EN_ESPERA");
    }

    @Test
    void obtener_DelegaAlServicio() {
        Turno t = Turno.builder().id(1L).numeroTurno("T-1001").build();
        when(turnoService.obtener(1L)).thenReturn(t);

        assertEquals("T-1001", turnoController.obtener(1L).getNumeroTurno());
    }

    @Test
    void crear_DelegaAlServicio() {
        CrearTurnoRequest req = new CrearTurnoRequest("Camila", 3, true);
        Turno creado = Turno.builder().id(1L).numeroTurno("T-1001").build();
        when(turnoService.crear(req)).thenReturn(creado);

        assertEquals("T-1001", turnoController.crear(req).getNumeroTurno());
    }

    @Test
    void atenderSiguiente_DelegaAlServicio() {
        Turno atendido = Turno.builder().id(1L).estado("ATENDIDO").build();
        when(turnoService.atenderSiguiente()).thenReturn(atendido);

        assertEquals("ATENDIDO", turnoController.atenderSiguiente().getEstado());
    }

    @Test
    void cambiarEstado_DelegaAlServicio() {
        Turno actualizado = Turno.builder().id(1L).estado("CANCELADO").build();
        when(turnoService.cambiarEstado(1L, "CANCELADO")).thenReturn(actualizado);

        assertEquals("CANCELADO", turnoController.cambiarEstado(1L, new CambiarEstadoTurnoRequest("CANCELADO")).getEstado());
    }
}