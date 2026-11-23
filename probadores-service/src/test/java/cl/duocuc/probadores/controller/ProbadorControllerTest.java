package cl.duocuc.probadores.controller;

import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CambiarEstadoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.service.ProbadorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProbadorControllerTest {

    @Mock
    private ProbadorService probadorService;

    @InjectMocks
    private ProbadorController probadorController;

    @Test
    void listar_DelegaAlServicio() {
        when(probadorService.listar("LIBRE")).thenReturn(List.of(Probador.builder().codigo("P-01").esAccesible(false).build()));

        List<Probador> res = probadorController.listar("LIBRE");

        assertEquals(1, res.size());
        verify(probadorService).listar("LIBRE");
    }

    @Test
    void obtener_DelegaAlServicio() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).build();
        when(probadorService.obtener("P-01")).thenReturn(p);

        assertEquals("P-01", probadorController.obtener("P-01").getCodigo());
    }

    @Test
    void crear_DelegaAlServicio() {
        CrearProbadorRequest req = new CrearProbadorRequest("P-04", true);
        Probador creado = Probador.builder().codigo("P-04").esAccesible(true).build();
        when(probadorService.crear(req)).thenReturn(creado);

        assertEquals("P-04", probadorController.crear(req).getCodigo());
    }

    @Test
    void cambiarEstado_DelegaAlServicio() {
        Probador actualizado = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").build();
        when(probadorService.cambiarEstado("P-01", "OCUPADO")).thenReturn(actualizado);

        assertEquals("OCUPADO", probadorController.cambiarEstado("P-01", new CambiarEstadoProbadorRequest("OCUPADO")).getEstado());
    }

    @Test
    void cambiarBloqueo_DelegaAlServicio() {
        BloqueoProbadorRequest req = new BloqueoProbadorRequest(true, "Mantenimiento");
        Probador actualizado = Probador.builder().codigo("P-01").esAccesible(false).estado("FUERA_SERVICIO").build();
        when(probadorService.cambiarBloqueo("P-01", req)).thenReturn(actualizado);

        assertEquals("FUERA_SERVICIO", probadorController.cambiarBloqueo("P-01", req).getEstado());
    }

    @Test
    void eliminar_DelegaAlServicio() {
        probadorController.eliminar("P-01");
        verify(probadorService).eliminar("P-01");
    }

    @Test
    void historial_DelegaAlServicio() {
        when(probadorService.historial("P-01")).thenReturn(java.util.List.of(
                new cl.duocuc.probadores.dto.UsoProbadorDto("T-1", "Ana", java.time.LocalDateTime.now(), null)));

        assertEquals("Ana", probadorController.historial("P-01").get(0).nombreCliente());
    }
}