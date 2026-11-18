package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private final UsuarioResponse sample = new UsuarioResponse(1L, "admin", "Admin", "a@b.cl", 1L, "ADMIN", false, false);

    @Test
    void todosLosEndpoints_DeleganCorrectamenteAlServicio() {
        LoginRequest loginReq = new LoginRequest("admin", "admin1234");
        when(usuarioService.login(loginReq)).thenReturn(sample);
        assertEquals("admin", usuarioController.login(loginReq).username());

        when(usuarioService.listar("ad", 1L)).thenReturn(List.of(sample));
        assertEquals(1, usuarioController.listar("ad", 1L).size());

        when(usuarioService.obtenerPorId(1L)).thenReturn(sample);
        assertEquals(1L, usuarioController.obtenerPorId(1L).id());

        when(usuarioService.obtenerPorUsername("admin")).thenReturn(sample);
        assertEquals("admin", usuarioController.obtenerPorUsername("admin").username());

        RegistroUsuarioRequest regReq = new RegistroUsuarioRequest("user", "User", "u@b.cl", null, 2L);
        when(usuarioService.crear(regReq)).thenReturn(sample);
        assertEquals("admin", usuarioController.crear(regReq).username());

        ActualizarUsuarioRequest actReq = new ActualizarUsuarioRequest("user", "User", "u@b.cl", 2L, null);
        when(usuarioService.actualizar(1L, actReq)).thenReturn(sample);
        assertEquals("admin", usuarioController.actualizar(1L, actReq).username());

        CambiarContrasenaRequest passReq = new CambiarContrasenaRequest("old12345", "new12345");
        usuarioController.cambiarContrasena(1L, passReq);
        verify(usuarioService).cambiarContrasena(1L, passReq);

        usuarioController.recuperarContrasena(new RecuperarContrasenaRequest("admin"));
        verify(usuarioService).recuperarContrasena("admin");

        when(usuarioService.cambiarBloqueo(2L, true, 1L)).thenReturn(sample);
        assertEquals("admin", usuarioController.cambiarBloqueo(2L, new BloqueoRequest(true), 1L).username());

        usuarioController.eliminar(2L, 1L);
        verify(usuarioService).eliminar(2L, 1L);
    }
}