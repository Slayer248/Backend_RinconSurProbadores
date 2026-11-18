package cl.duocuc.probadores.service;

import cl.duocuc.probadores.client.TurnoClient;
import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.dto.TurnoAtendidoResponse;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    void listar_SinFiltro_RetornaTodosOrdenados() {
        when(probadorRepository.findAllByOrderByCodigoAsc()).thenReturn(List.of(
                Probador.builder().codigo("P-01").esAccesible(false).build()
        ));

        List<Probador> resultado = probadorService.listar(null);

        assertEquals(1, resultado.size());
        verify(probadorRepository).findAllByOrderByCodigoAsc();
    }

    @Test
    void listar_ConFiltroEstado_RetornaFiltrados() {
        when(probadorRepository.findByEstadoIgnoreCaseOrderByCodigoAsc("libre")).thenReturn(List.of(
                Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").build()
        ));

        List<Probador> resultado = probadorService.listar("  libre ");

        assertEquals(1, resultado.size());
        verify(probadorRepository).findByEstadoIgnoreCaseOrderByCodigoAsc("libre");
    }

    @Test
    void obtener_ExitosoNormalizaCodigo() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        Probador resultado = probadorService.obtener(" p-01 ");

        assertEquals("P-01", resultado.getCodigo());
    }

    @Test
    void obtener_NoExiste_LanzaNotFound() {
        when(probadorRepository.findById("P-99")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.obtener("P-99")
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void crear_ExitosoNormalizaCodigoYPorDefectoLibre() {
        when(probadorRepository.existsById("P-04")).thenReturn(false);
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador creado = probadorService.crear(new CrearProbadorRequest(" p-04 ", true));

        assertEquals("P-04", creado.getCodigo());
        assertTrue(creado.getEsAccesible());
        assertEquals("LIBRE", creado.getEstado());
        assertEquals("-", creado.getDesdeHora());
    }

    @Test
    void crear_CodigoCortoONulo_LanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () ->
                probadorService.crear(new CrearProbadorRequest(null, false))
        );
        assertThrows(ResponseStatusException.class, () ->
                probadorService.crear(new CrearProbadorRequest("A", false))
        );
        verify(probadorRepository, never()).save(any());
    }

    @Test
    void crear_CodigoDuplicado_LanzaConflict() {
        when(probadorRepository.existsById("P-01")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.crear(new CrearProbadorRequest("p-01", false))
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void cambiarEstado_AOcupado_AsignaHoraYRegistraUltimoCliente() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").desdeHora("-").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(turnoClient.atenderSiguiente()).thenReturn(Optional.of(new TurnoAtendidoResponse("T-1001", "Ana Morales")));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador resultado = probadorService.cambiarEstado("P-01", "ocupado");

        assertEquals("OCUPADO", resultado.getEstado());
        assertNotEquals("-", resultado.getDesdeHora());
        assertEquals("Ana Morales", resultado.getUltimoCliente());
        assertEquals("T-1001", resultado.getUltimoTurno());
        assertNotNull(resultado.getUltimoUso());
        verify(turnoClient).atenderSiguiente();
    }

    @Test
    void cambiarEstado_AOcupadoSinClientesEnFila_LanzaConflictYNoGuarda() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").desdeHora("-").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(turnoClient.atenderSiguiente()).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.cambiarEstado("P-01", "OCUPADO")
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("LIBRE", p.getEstado());
        verify(probadorRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_YaOcupado_NoConsumeOtroTurno() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").desdeHora("14:30").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        Probador resultado = probadorService.cambiarEstado("P-01", "OCUPADO");

        assertEquals("14:30", resultado.getDesdeHora());
        verify(turnoClient, never()).atenderSiguiente();
    }

    @Test
    void cambiarEstado_ALibre_ConservaUltimoCliente() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIMPIEZA")
                .ultimoCliente("Ana Morales").ultimoTurno("T-1001").ultimoUso("18-11-2026 10:15").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador resultado = probadorService.cambiarEstado("P-01", "LIBRE");

        assertEquals("Ana Morales", resultado.getUltimoCliente());
    }

    @Test
    void cambiarEstado_ALibre_ReseteaHoraSinLlamarTurnos() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").desdeHora("14:30").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador resultado = probadorService.cambiarEstado("P-01", "libre");

        assertEquals("LIBRE", resultado.getEstado());
        assertEquals("-", resultado.getDesdeHora());
        verify(turnoClient, never()).atenderSiguiente();
    }

    @Test
    void cambiarEstado_FueraDeServicio_LanzaConflict() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("FUERA_SERVICIO").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.cambiarEstado("P-01", "LIBRE")
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void cambiarEstado_EstadoVacio_LanzaBadRequest() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.cambiarEstado("P-01", "   ")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cambiarBloqueo_BloquearLibre_MarcaFueraServicioConMotivo() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador resultado = probadorService.cambiarBloqueo("P-01", new BloqueoProbadorRequest(true, "  Puerta rota "));

        assertEquals("FUERA_SERVICIO", resultado.getEstado());
        assertEquals("Puerta rota", resultado.getMotivoFueraServicio());
        assertEquals("-", resultado.getDesdeHora());
    }

    @Test
    void cambiarBloqueo_Desbloquear_MarcaLibreYLimpiaMotivo() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("FUERA_SERVICIO").motivoFueraServicio("Roto").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));
        when(probadorRepository.save(any(Probador.class))).thenAnswer(inv -> inv.getArgument(0));

        Probador resultado = probadorService.cambiarBloqueo("P-01", new BloqueoProbadorRequest(false, null));

        assertEquals("LIBRE", resultado.getEstado());
        assertNull(resultado.getMotivoFueraServicio());
    }

    @Test
    void cambiarBloqueo_BloquearOcupado_LanzaConflict() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.cambiarBloqueo("P-01", new BloqueoProbadorRequest(true, "Limpieza"))
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void eliminar_ProbadorLibre_EliminaCorrectamente() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        probadorService.eliminar("P-01");

        verify(probadorRepository).delete(p);
    }

    @Test
    void eliminar_ProbadorOcupado_LanzaConflict() {
        Probador p = Probador.builder().codigo("P-01").esAccesible(false).estado("OCUPADO").build();
        when(probadorRepository.findById("P-01")).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                probadorService.eliminar("P-01")
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(probadorRepository, never()).delete(any());
    }
}