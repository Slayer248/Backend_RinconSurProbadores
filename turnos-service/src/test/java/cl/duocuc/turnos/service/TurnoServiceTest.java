package cl.duocuc.turnos.service;

import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.repository.TurnoRepository;
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
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private TurnoService turnoService;

    @Test
    void listarPorEstado_EstadoNuloOVacio_UsaEnEsperaPorDefecto() {
        when(turnoRepository.findByEstadoOrderByEsPreferenteDescIdAsc("EN_ESPERA"))
                .thenReturn(List.of(Turno.builder().id(1L).numeroTurno("T-1001").build()));

        List<Turno> resNulo = turnoService.listarPorEstado(null);
        List<Turno> resVacio = turnoService.listarPorEstado("   ");

        assertEquals(1, resNulo.size());
        assertEquals(1, resVacio.size());
        verify(turnoRepository, times(2)).findByEstadoOrderByEsPreferenteDescIdAsc("EN_ESPERA");
    }

    @Test
    void listarPorEstado_ConEstadoExplicito_NormalizaAMayusculas() {
        when(turnoRepository.findByEstadoOrderByEsPreferenteDescIdAsc("ATENDIDO"))
                .thenReturn(List.of(Turno.builder().id(1L).estado("ATENDIDO").build()));

        List<Turno> res = turnoService.listarPorEstado("  atendido ");

        assertEquals(1, res.size());
        verify(turnoRepository).findByEstadoOrderByEsPreferenteDescIdAsc("ATENDIDO");
    }

    @Test
    void obtener_ExitosoRetornaTurno() {
        Turno turno = Turno.builder().id(1L).numeroTurno("T-1001").build();
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        Turno resultado = turnoService.obtener(1L);

        assertEquals("T-1001", resultado.getNumeroTurno());
    }

    @Test
    void obtener_NoExiste_LanzaNotFound() {
        when(turnoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                turnoService.obtener(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void crear_ExitosoGeneraCorrelativoYNormaliza() {
        when(turnoRepository.existsByNombreClienteIgnoreCaseAndEstado("Camila", "EN_ESPERA")).thenReturn(false);
        when(turnoRepository.count()).thenReturn(2L);
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        Turno creado = turnoService.crear(new CrearTurnoRequest("  Camila ", 4, true));

        assertEquals("T-1003", creado.getNumeroTurno());
        assertEquals("Camila", creado.getNombreCliente());
        assertEquals(4, creado.getCantidadPrendas());
        assertTrue(creado.getEsPreferente());
        assertEquals("EN_ESPERA", creado.getEstado());
    }

    @Test
    void crear_NombreInvalido_LanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest(null, 3, false))
        );
        assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest("   ", 3, false))
        );
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void crear_CantidadPrendasFueraDeRango_LanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest("Juan", null, false))
        );
        assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest("Juan", 0, false))
        );
        assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest("Juan", 7, false))
        );
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void crear_ClienteConTurnoActivo_LanzaConflict() {
        when(turnoRepository.existsByNombreClienteIgnoreCaseAndEstado("Camila", "EN_ESPERA")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                turnoService.crear(new CrearTurnoRequest("Camila", 2, false))
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void atenderSiguiente_ExitosoMarcaAtendido() {
        Turno siguiente = Turno.builder().id(1L).numeroTurno("T-1001").estado("EN_ESPERA").build();
        when(turnoRepository.findFirstByEstadoOrderByEsPreferenteDescIdAsc("EN_ESPERA"))
                .thenReturn(Optional.of(siguiente));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        Turno resultado = turnoService.atenderSiguiente();

        assertEquals("ATENDIDO", resultado.getEstado());
        verify(turnoRepository).save(siguiente);
    }

    @Test
    void atenderSiguiente_SinTurnosEnEspera_LanzaNotFound() {
        when(turnoRepository.findFirstByEstadoOrderByEsPreferenteDescIdAsc("EN_ESPERA"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                turnoService.atenderSiguiente()
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void cambiarEstado_ExitosoActualizaEnMayusculas() {
        Turno turno = Turno.builder().id(1L).estado("EN_ESPERA").build();
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        Turno resultado = turnoService.cambiarEstado(1L, " cancelado ");

        assertEquals("CANCELADO", resultado.getEstado());
    }

    @Test
    void cambiarEstado_EstadoNuloOVacio_LanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> turnoService.cambiarEstado(1L, null));
        assertThrows(ResponseStatusException.class, () -> turnoService.cambiarEstado(1L, "  "));
        verify(turnoRepository, never()).save(any());
    }
}