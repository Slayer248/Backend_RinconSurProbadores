package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .nombre("Administrador Principal")
                    .correo("admin@rinconsur.cl")
                    .contrasena(encoder.encode("admin1234"))
                    .rolId(1L)
                    .bloqueado(false)
                    .build());
        }
    }
}