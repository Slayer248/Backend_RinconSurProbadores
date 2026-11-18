package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.RegistroUsuarioRequest;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolClient rolClient;

    @InjectMocks
    private UsuarioService usuarioService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void login_CredencialesValidasRetornaUsuarioConRol() {
        Usuario user = Usuario.builder()
                .id(1L).username("admin").nombre("Admin").correo("admin@rinconsur.cl")
                .contrasena(encoder.encode("admin1234")).rolId(1L).bloqueado(false).build();

        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(rolClient.obtenerPorId(1L)).thenReturn(new RolDto(1L, "ADMIN"));

        UsuarioResponse res = usuarioService.login(new LoginRequest("admin", "admin1234"));

        assertEquals("admin", res.username());
        assertEquals("ADMIN", res.rolNombre());
    }

    @Test
    void login_CuentaBloqueadaLanzaForbidden() {
        Usuario user = Usuario.builder()
                .id(2L).username("asistente1").contrasena(encoder.encode("Clave1234"))
                .rolId(2L).bloqueado(true).build();

        when(usuarioRepository.findByUsernameIgnoreCase("asistente1")).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () ->
                usuarioService.login(new LoginRequest("asistente1", "Clave1234"))
        );
    }

    @Test
    void crearUsuario_ContrasenaDebilLanzaBadRequest() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest(
                "nuevo", "Nuevo Usuario", "nuevo@rinconsur.cl", "1234", 2L
        );

        assertThrows(ResponseStatusException.class, () -> usuarioService.crear(req));
        verify(usuarioRepository, never()).save(any());
    }
}