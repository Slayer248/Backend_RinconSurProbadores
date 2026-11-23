package cl.duocuc.probadoresinhabilitados.controller;

import cl.duocuc.probadoresinhabilitados.dto.ArchivarProbadorRequest;
import cl.duocuc.probadoresinhabilitados.dto.ProbadorInhabilitadoResponse;
import cl.duocuc.probadoresinhabilitados.service.ProbadorInhabilitadoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProbadorInhabilitadoControllerTest {

    @Mock
    private ProbadorInhabilitadoService service;

    @InjectMocks
    private ProbadorInhabilitadoController controller;

    private final ProbadorInhabilitadoResponse sample = new ProbadorInhabilitadoResponse(
            3L, "P-01", true, "LIBRE", null, "Ana", "T-2", "20-11-2026 11:00", LocalDateTime.now(), List.of());

    @Test
    void todosLosEndpoints_DeleganCorrectamenteAlServicio() {
        ArchivarProbadorRequest req = new ArchivarProbadorRequest("P-01", true, "LIBRE", null, "Ana", "T-2", "x", List.of());
        when(service.archivar(req)).thenReturn(sample);
        assertEquals(3L, controller.archivar(req).id());
        verify(service).archivar(req);

        when(service.listar()).thenReturn(List.of(sample));
        assertEquals(1, controller.listar().size());

        when(service.obtener(3L)).thenReturn(sample);
        assertEquals("P-01", controller.obtener(3L).codigo());
    }
}
