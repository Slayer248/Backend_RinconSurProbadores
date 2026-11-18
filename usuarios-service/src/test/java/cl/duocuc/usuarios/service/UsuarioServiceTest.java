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

    @Mock
    private EmailService emailService;

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
    void crearUsuario_CamposInvalidosLanzaBadRequest() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest(
                "ab", "Nuevo", "nuevo@rinconsur.cl", null, 2L
        );

        assertThrows(ResponseStatusException.class, () -> usuarioService.crear(req));
        verify(usuarioRepository, never()).save(any());
        verify(emailService, never()).enviarCredenciales(any(), any(), any(), any(), any());
    }

    @Test
    void crearUsuario_ExitoGeneraContrasenaYEnviaCorreo() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest(
                "nuevo", "Nuevo Usuario", "nuevo@rinconsur.cl", null, 2L
        );

        when(usuarioRepository.existsByUsernameIgnoreCase("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("nuevo@rinconsur.cl")).thenReturn(false);
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "ASISTENTE"));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            return Usuario.builder()
                    .id(10L)
                    .username(u.getUsername())
                    .nombre(u.getNombre())
                    .correo(u.getCorreo())
                    .contrasena(u.getContrasena())
                    .rolId(u.getRolId())
                    .bloqueado(u.getBloqueado())
                    .build();
        });

        UsuarioResponse res = usuarioService.crear(req);

        assertNotNull(res);
        assertEquals("nuevo", res.username());
        assertEquals("ASISTENTE", res.rolNombre());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(emailService).enviarCredenciales(eq("nuevo@rinconsur.cl"), eq("Nuevo Usuario"), eq("nuevo"), anyString(), eq("ASISTENTE"));
    }

    @Test
    void recuperarContrasena_ExitoGeneraClaveYEnviaCorreo() {
        Usuario user = Usuario.builder()
                .id(5L).username("juan").nombre("Juan").correo("juan@rinconsur.cl")
                .contrasena(encoder.encode("ViejaClave1")).rolId(2L).bloqueado(false).build();

        when(usuarioRepository.findByUsernameIgnoreCase("juan")).thenReturn(Optional.of(user));

        usuarioService.recuperarContrasena("juan");

        verify(usuarioRepository).save(user);
        verify(emailService).enviarRecuperacionContrasena(eq("juan@rinconsur.cl"), eq("Juan"), eq("juan"), anyString());
    }

    @Test
    void recuperarContrasena_UsuarioNoExisteLanzaNotFound() {
        when(usuarioRepository.findByUsernameIgnoreCase("fantasma")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> usuarioService.recuperarContrasena("fantasma"));
        verify(usuarioRepository, never()).save(any());
        verify(emailService, never()).enviarRecuperacionContrasena(any(), any(), any(), any());
    }
}