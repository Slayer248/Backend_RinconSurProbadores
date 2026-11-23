package cl.duocuc.cuentasinhabilitadas.service;

import cl.duocuc.cuentasinhabilitadas.dto.ArchivarCuentaRequest;
import cl.duocuc.cuentasinhabilitadas.dto.CuentaInhabilitadaResponse;
import cl.duocuc.cuentasinhabilitadas.model.CuentaInhabilitada;
import cl.duocuc.cuentasinhabilitadas.repository.CuentaInhabilitadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class CuentaInhabilitadaServiceTest {

    @Mock
    private CuentaInhabilitadaRepository repository;

    @InjectMocks
    private CuentaInhabilitadaService service;

    private ArchivarCuentaRequest request() {
        return new ArchivarCuentaRequest(5L, "mgonzalez", "Maria Gonzalez", "mg@rinconsur.test",
                "hash", 2L, "ASISTENTE", false, 1L);
    }

    private CuentaInhabilitada entidad() {
        return CuentaInhabilitada.builder()
                .id(10L).usuarioId(5L).username("mgonzalez").nombre("Maria Gonzalez")
                .correo("mg@rinconsur.test").contrasena("hash").rolId(2L).rolNombre("ASISTENTE")
                .bloqueadaAntes(false).inhabilitadaPorId(1L)
                .fechaInhabilitacion(LocalDateTime.now()).build();
    }

    @Test
    void archivar_CuentaNueva_GuardaTodosLosDatos() {
        when(repository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        when(repository.save(any(CuentaInhabilitada.class))).thenAnswer(inv -> {
            CuentaInhabilitada c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CuentaInhabilitadaResponse res = service.archivar(request());

        assertEquals(10L, res.id());
        assertEquals(5L, res.usuarioId());
        assertEquals("mgonzalez", res.username());
        assertEquals("ASISTENTE", res.rolNombre());
        assertEquals(1L, res.inhabilitadaPorId());
        assertNotNull(res.fechaInhabilitacion());
        verify(repository).save(argThat(c -> "hash".equals(c.getContrasena()) && !c.getBloqueadaAntes()));
    }

    @Test
    void archivar_BloqueadaAntesNulo_SeGuardaComoFalso() {
        when(repository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        when(repository.save(any(CuentaInhabilitada.class))).thenAnswer(inv -> inv.getArgument(0));
        ArchivarCuentaRequest r = new ArchivarCuentaRequest(5L, "mgonzalez", "Maria Gonzalez",
                "mg@rinconsur.test", "hash", 2L, "ASISTENTE", null, null);

        assertFalse(service.archivar(r).bloqueadaAntes());
    }

    @Test
    void archivar_CuentaYaArchivada_EsIdempotenteYNoDuplica() {
        when(repository.findByUsuarioId(5L)).thenReturn(Optional.of(entidad()));

        CuentaInhabilitadaResponse res = service.archivar(request());

        assertEquals(10L, res.id());
        verify(repository, never()).save(any());
    }

    @Test
    void archivar_DatosIncompletos_LanzaBadRequest() {
        ArchivarCuentaRequest sinId = new ArchivarCuentaRequest(null, "u", "n", "c", "p", 2L, "R", false, 1L);
        ArchivarCuentaRequest sinUsername = new ArchivarCuentaRequest(5L, " ", "n", "c", "p", 2L, "R", false, 1L);
        ArchivarCuentaRequest sinNombre = new ArchivarCuentaRequest(5L, "u", null, "c", "p", 2L, "R", false, 1L);
        ArchivarCuentaRequest sinCorreo = new ArchivarCuentaRequest(5L, "u", "n", "", "p", 2L, "R", false, 1L);
        ArchivarCuentaRequest sinClave = new ArchivarCuentaRequest(5L, "u", "n", "c", null, 2L, "R", false, 1L);
        ArchivarCuentaRequest sinRol = new ArchivarCuentaRequest(5L, "u", "n", "c", "p", null, "R", false, 1L);

        for (ArchivarCuentaRequest r : List.of(sinId, sinUsername, sinNombre, sinCorreo, sinClave, sinRol)) {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.archivar(r));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void listar_RetornaCuentasSinExponerContrasena() {
        when(repository.findAllByOrderByFechaInhabilitacionDesc()).thenReturn(List.of(entidad()));

        List<CuentaInhabilitadaResponse> lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals("mgonzalez", lista.get(0).username());
        assertTrue(java.util.Arrays.stream(CuentaInhabilitadaResponse.class.getRecordComponents())
                .noneMatch(c -> c.getName().toLowerCase().contains("contrasena")));
    }

    @Test
    void obtener_Existente_Retorna() {
        when(repository.findById(10L)).thenReturn(Optional.of(entidad()));
        assertEquals(10L, service.obtener(10L).id());
    }

    @Test
    void obtener_Inexistente_LanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.obtener(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
