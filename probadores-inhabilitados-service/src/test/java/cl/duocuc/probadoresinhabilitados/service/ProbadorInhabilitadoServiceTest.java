package cl.duocuc.probadoresinhabilitados.service;

import cl.duocuc.probadoresinhabilitados.dto.ArchivarProbadorRequest;
import cl.duocuc.probadoresinhabilitados.dto.ProbadorInhabilitadoResponse;
import cl.duocuc.probadoresinhabilitados.dto.UsoProbadorDto;
import cl.duocuc.probadoresinhabilitados.model.ProbadorInhabilitado;
import cl.duocuc.probadoresinhabilitados.model.UsoProbadorArchivado;
import cl.duocuc.probadoresinhabilitados.repository.ProbadorInhabilitadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProbadorInhabilitadoServiceTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 11, 20, 10, 0);

    @Mock
    private ProbadorInhabilitadoRepository repository;

    @InjectMocks
    private ProbadorInhabilitadoService service;

    private ArchivarProbadorRequest request(List<UsoProbadorDto> usos) {
        return new ArchivarProbadorRequest("P-01", true, "LIBRE", null, "Ana Morales", "T-2", "20-11-2026 11:00", usos);
    }

    private ProbadorInhabilitado entidad() {
        ProbadorInhabilitado p = ProbadorInhabilitado.builder()
                .id(3L).codigo("P-01").esAccesible(true).estadoAnterior("LIBRE")
                .fechaInhabilitacion(T).build();
        p.getUsos().add(UsoProbadorArchivado.builder().probador(p).codigoProbador("P-01")
                .numeroTurno("T-1").nombreCliente("Luis").inicio(T).fin(T.plusMinutes(7)).build());
        return p;
    }

    @Test
    void archivar_GuardaProbadorConTodosLosInvitados() {
        when(repository.save(any(ProbadorInhabilitado.class))).thenAnswer(inv -> {
            ProbadorInhabilitado p = inv.getArgument(0);
            p.setId(3L);
            return p;
        });

        ProbadorInhabilitadoResponse res = service.archivar(request(List.of(
                new UsoProbadorDto("T-1", "Luis", T, T.plusMinutes(7)),
                new UsoProbadorDto("T-2", "Ana Morales", T.plusHours(1), null)
        )));

        assertEquals(3L, res.id());
        assertEquals("P-01", res.codigo());
        assertTrue(res.esAccesible());
        assertEquals(2, res.usos().size());
        assertEquals("Luis", res.usos().get(0).nombreCliente());
        assertEquals("Ana Morales", res.usos().get(1).nombreCliente());

        ArgumentCaptor<ProbadorInhabilitado> guardado = ArgumentCaptor.forClass(ProbadorInhabilitado.class);
        verify(repository).save(guardado.capture());
        assertNotNull(guardado.getValue().getFechaInhabilitacion());
        assertEquals("P-01", guardado.getValue().getUsos().get(0).getCodigoProbador());
        assertSame(guardado.getValue(), guardado.getValue().getUsos().get(0).getProbador());
    }

    @Test
    void archivar_SinUsos_GuardaIgual() {
        when(repository.save(any(ProbadorInhabilitado.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.archivar(request(null)).usos().isEmpty());
        assertTrue(service.archivar(request(List.of())).usos().isEmpty());
    }

    @Test
    void archivar_DatosDelProbadorIncompletos_LanzaBadRequest() {
        ArchivarProbadorRequest sinCodigo = new ArchivarProbadorRequest(" ", true, "LIBRE", null, null, null, null, List.of());
        ArchivarProbadorRequest codigoNulo = new ArchivarProbadorRequest(null, true, "LIBRE", null, null, null, null, List.of());
        ArchivarProbadorRequest sinAccesible = new ArchivarProbadorRequest("P-01", null, "LIBRE", null, null, null, null, List.of());

        for (ArchivarProbadorRequest r : List.of(sinCodigo, codigoNulo, sinAccesible)) {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.archivar(r));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void archivar_UsoSinNombreOSinInicio_LanzaBadRequest() {
        List<List<UsoProbadorDto>> invalidos = List.of(
                List.of(new UsoProbadorDto("T-1", null, T, null)),
                List.of(new UsoProbadorDto("T-1", " ", T, null)),
                List.of(new UsoProbadorDto("T-1", "Luis", null, null))
        );

        for (List<UsoProbadorDto> usos : invalidos) {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.archivar(request(usos)));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void listar_RetornaProbadoresConSusUsos() {
        when(repository.findAllByOrderByFechaInhabilitacionDesc()).thenReturn(List.of(entidad()));

        List<ProbadorInhabilitadoResponse> lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals("Luis", lista.get(0).usos().get(0).nombreCliente());
    }

    @Test
    void obtener_Existente_Retorna() {
        when(repository.findById(3L)).thenReturn(Optional.of(entidad()));
        assertEquals("P-01", service.obtener(3L).codigo());
    }

    @Test
    void obtener_Inexistente_LanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.obtener(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
