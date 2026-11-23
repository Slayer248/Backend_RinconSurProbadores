package cl.duocuc.cuentasinhabilitadas.controller;

import cl.duocuc.cuentasinhabilitadas.dto.ArchivarCuentaRequest;
import cl.duocuc.cuentasinhabilitadas.dto.CuentaInhabilitadaResponse;
import cl.duocuc.cuentasinhabilitadas.service.CuentaInhabilitadaService;
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
class CuentaInhabilitadaControllerTest {

    @Mock
    private CuentaInhabilitadaService service;

    @InjectMocks
    private CuentaInhabilitadaController controller;

    private final CuentaInhabilitadaResponse sample = new CuentaInhabilitadaResponse(
            10L, 5L, "mgonzalez", "Maria Gonzalez", "mg@rinconsur.test", 2L, "ASISTENTE",
            false, 1L, LocalDateTime.now());

    @Test
    void todosLosEndpoints_DeleganCorrectamenteAlServicio() {
        ArchivarCuentaRequest req = new ArchivarCuentaRequest(5L, "mgonzalez", "Maria Gonzalez",
                "mg@rinconsur.test", "hash", 2L, "ASISTENTE", false, 1L);
        when(service.archivar(req)).thenReturn(sample);
        assertEquals(10L, controller.archivar(req).id());
        verify(service).archivar(req);

        when(service.listar()).thenReturn(List.of(sample));
        assertEquals(1, controller.listar().size());

        when(service.obtener(10L)).thenReturn(sample);
        assertEquals("mgonzalez", controller.obtener(10L).username());
    }
}
