package cl.duocuc.cuentasinhabilitadas;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("No requiere levantar contexto con BD real en pruebas unitarias")
class CuentasInhabilitadasServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
