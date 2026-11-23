package cl.duocuc.cuentasinhabilitadas.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_DefineTituloYVersion() {
        OpenAPI api = new OpenApiConfig().customOpenAPI();

        assertNotNull(api.getInfo());
        assertEquals("1.0", api.getInfo().getVersion());
        assertEquals("API Microservicio de Cuentas Inhabilitadas - Rincón Sur", api.getInfo().getTitle());
    }
}
