package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final long ROL_ADMIN = 1L;
    private static final long ROL_ASISTENTE = 2L;
    private static final String CLAVE_DEMO = "Rincon2026!";

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.datos-iniciales.demo:true}")
    private boolean cargarDemo = true;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .nombre("Administrador Principal")
                    .correo("admin@rinconsur.cl")
                    .contrasena(encoder.encode("admin1234"))
                    .rolId(ROL_ADMIN)
                    .bloqueado(false)
                    .debeCambiarContrasena(true)
                    .build());
        }

        if (cargarDemo) {
            crearAsistenteSiNoExiste("mgonzalez", "Maria Gonzalez", "mgonzalez@rinconsur.test");
            crearAsistenteSiNoExiste("pmunoz", "Pedro Munoz", "pmunoz@rinconsur.test");
        }
    }

    private void crearAsistenteSiNoExiste(String username, String nombre, String correo) {
        if (usuarioRepository.existsByUsernameIgnoreCase(username) || usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .username(username)
                .nombre(nombre)
                .correo(correo)
                .contrasena(encoder.encode(CLAVE_DEMO))
                .rolId(ROL_ASISTENTE)
                .bloqueado(false)
                .debeCambiarContrasena(true)
                .build());
    }
}
