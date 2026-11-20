package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolClient rolClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void login_ExitosoRetornaUsuarioConRol() {
        Usuario u = Usuario.builder()
                .id(1L).username("admin").nombre("Admin").correo("a@b.cl")
                .contrasena(encoder.encode("admin1234")).rolId(1L).bloqueado(false)
                .build();
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(u));
        when(rolClient.obtenerPorId(1L)).thenReturn(new RolDto(1L, "ADMIN"));

        UsuarioResponse res = usuarioService.login(new LoginRequest(" admin ", "admin1234"));

        assertEquals("admin", res.username());
        assertEquals("ADMIN", res.rolNombre());
    }

    @Test
    void login_CamposVacios_LanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> usuarioService.login(new LoginRequest("", "123")));
        assertThrows(ResponseStatusException.class, () -> usuarioService.login(new LoginRequest("admin", null)));
    }

    @Test
    void login_CredencialesInvalidas_LanzaUnauthorized() {
        when(usuarioRepository.findByUsernameIgnoreCase("noexiste")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> usuarioService.login(new LoginRequest("noexiste", "1234")));

        Usuario u = Usuario.builder().username("admin").contrasena(encoder.encode("correcta1")).bloqueado(false).build();
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(u));
        assertThrows(ResponseStatusException.class, () -> usuarioService.login(new LoginRequest("admin", "erronea1")));
    }

    @Test
    void login_UsuarioBloqueado_LanzaForbidden() {
        Usuario u = Usuario.builder().username("user").contrasena(encoder.encode("pass1234")).bloqueado(true).build();
        when(usuarioRepository.findByUsernameIgnoreCase("user")).thenReturn(Optional.of(u));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                usuarioService.login(new LoginRequest("user", "pass1234"))
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void listar_FiltraPorTextoYRol() {
        when(rolClient.obtenerMapaRoles()).thenReturn(Map.of(1L, "ADMIN", 2L, "ASISTENTE"));
        when(usuarioRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                Usuario.builder().id(1L).username("admin").nombre("Admin").correo("a@b.cl").rolId(1L).bloqueado(false).build(),
                Usuario.builder().id(2L).username("maria").nombre("María Pérez").correo("m@b.cl").rolId(2L).bloqueado(false).build()
        ));

        List<UsuarioResponse> todos = usuarioService.listar(null, null);
        List<UsuarioResponse> filtrados = usuarioService.listar("maría", 2L);

        assertEquals(2, todos.size());
        assertEquals(1, filtrados.size());
        assertEquals("ASISTENTE", filtrados.get(0).rolNombre());
    }

    @Test
    void obtenerPorIdYUsername_ExitososYNotFound() {
        Usuario u = Usuario.builder().id(1L).username("admin").nombre("Admin").correo("a@b.cl").rolId(1L).bloqueado(false).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.findByUsernameIgnoreCase("ADMIN")).thenReturn(Optional.of(u));
        when(rolClient.obtenerPorId(1L)).thenReturn(new RolDto(1L, "ADMIN"));

        assertEquals("admin", usuarioService.obtenerPorId(1L).username());
        assertEquals(1L, usuarioService.obtenerPorUsername("ADMIN").id());

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> usuarioService.obtenerPorId(99L));
    }

    @Test
    void crear_ExitosoEnviaCorreoYGuardaEncriptado() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest("jperez", "Juan Pérez", "juan@rinconsur.cl", null, 2L);
        when(usuarioRepository.existsByUsernameIgnoreCase("jperez")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@rinconsur.cl")).thenReturn(false);
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        UsuarioResponse res = usuarioService.crear(req);

        assertEquals(5L, res.id());
        assertEquals("ASISTENTE", res.rolNombre());
        assertTrue(res.debeCambiarContrasena());
        verify(emailService).enviarCredenciales(eq("juan@rinconsur.cl"), eq("Juan Pérez"), eq("jperez"), anyString(), eq("ASISTENTE"));
    }

    @Test
    void crear_ValidacionesYDuplicados_LanzanExcepcion() {
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("ab", "Nombre", "a@b.cl", null, 1L))
        );
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("abc", "No", "a@b.cl", null, 1L))
        );
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("abc", "Nombre", "correo-malo", null, 1L))
        );
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("abc", "Nombre", "a@b.cl", null, null))
        );

        when(usuarioRepository.existsByUsernameIgnoreCase("duplicado")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("duplicado", "Nombre", "a@b.cl", null, 1L))
        );

        when(usuarioRepository.existsByUsernameIgnoreCase("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("dup@b.cl")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.crear(new RegistroUsuarioRequest("nuevo", "Nombre", "dup@b.cl", null, 1L))
        );
    }

    @Test
    void crear_FalloEnvioCorreo_LanzaInternalServerError() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest("jperez", "Juan Pérez", "juan@rinconsur.cl", null, 2L);
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP error")).when(emailService).enviarCredenciales(any(), any(), any(), any(), any());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.crear(req));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void actualizar_ExitosoYProteccionesDeAdmin() {
        Usuario normal = Usuario.builder().id(2L).username("jperez").nombre("Juan").correo("j@b.cl").rolId(2L).build();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(normal));
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse res = usuarioService.actualizar(2L,
                new ActualizarUsuarioRequest("jperez2", "Juan Nuevo", "jn@b.cl", 2L, "NuevaPass123"));
        assertEquals("jperez2", res.username());
        // La contraseña no cambia por esta vía
        assertNull(normal.getContrasena());

        Usuario admin = Usuario.builder().id(1L).username("admin").nombre("Admin").correo("a@b.cl").rolId(1L).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.actualizar(1L, new ActualizarUsuarioRequest("otro", "Admin", "a@b.cl", 1L, null))
        );

        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.actualizar(1L, new ActualizarUsuarioRequest("admin", "Admin", "a@b.cl", 2L, null))
        );
    }

    @Test
    void cambiarContrasena_ExitosoYErrores() {
        Usuario u = Usuario.builder().id(2L).contrasena(encoder.encode("Actual123")).debeCambiarContrasena(true).build();
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        assertThrows(ResponseStatusException.class, () ->
                usuarioService.cambiarContrasena(2L, new CambiarContrasenaRequest("Incorrecta1", "NuevaPass1"))
        );
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.cambiarContrasena(2L, new CambiarContrasenaRequest("Actual123", "corta"))
        );
        assertThrows(ResponseStatusException.class, () ->
                usuarioService.cambiarContrasena(2L, new CambiarContrasenaRequest("Actual123", "Actual123"))
        );
        assertTrue(u.getDebeCambiarContrasena());

        usuarioService.cambiarContrasena(2L, new CambiarContrasenaRequest("Actual123", "NuevaPass123"));
        verify(usuarioRepository).save(u);
        assertFalse(u.getDebeCambiarContrasena());
    }

    @Test
    void recuperarContrasena_ExitosoYErrores() {
        Usuario u = Usuario.builder().id(2L).username("jperez").nombre("Juan").correo("j@b.cl").bloqueado(false).build();
        when(usuarioRepository.findByUsernameIgnoreCase("jperez")).thenReturn(Optional.of(u));

        usuarioService.recuperarContrasena("jperez");
        verify(usuarioRepository).save(u);
        assertTrue(u.getDebeCambiarContrasena());
        verify(emailService).enviarRecuperacionContrasena(eq("j@b.cl"), eq("Juan"), eq("jperez"), anyString());

        assertThrows(ResponseStatusException.class, () -> usuarioService.recuperarContrasena(" "));

        Usuario bloqueado = Usuario.builder().username("bloq").bloqueado(true).build();
        when(usuarioRepository.findByUsernameIgnoreCase("bloq")).thenReturn(Optional.of(bloqueado));
        assertThrows(ResponseStatusException.class, () -> usuarioService.recuperarContrasena("bloq"));
    }

    @Test
    void cambiarBloqueoYEliminar_ExitososYProtecciones() {
        Usuario admin = Usuario.builder().id(1L).username("admin").rolId(1L).build();
        Usuario asistente = Usuario.builder().id(2L).username("maria").rolId(2L).bloqueado(false).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(asistente));
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(ResponseStatusException.class, () -> usuarioService.cambiarBloqueo(1L, true, 1L));
        assertThrows(ResponseStatusException.class, () -> usuarioService.cambiarBloqueo(2L, true, 2L));

        UsuarioResponse bloqueado = usuarioService.cambiarBloqueo(2L, true, 1L);
        assertTrue(bloqueado.bloqueado());

        assertThrows(ResponseStatusException.class, () -> usuarioService.eliminar(1L, 1L));
        assertThrows(ResponseStatusException.class, () -> usuarioService.eliminar(2L, 2L));

        usuarioService.eliminar(2L, 1L);
        assertTrue(asistente.getBloqueado());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}