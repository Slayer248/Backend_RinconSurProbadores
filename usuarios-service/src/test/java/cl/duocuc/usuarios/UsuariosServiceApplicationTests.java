package cl.duocuc.usuarios;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("No requiere levantar contexto con BD real en pruebas unitarias")
class UsuariosServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}