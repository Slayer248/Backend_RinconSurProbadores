package cl.duocuc.roles.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void testCustomOpenAPI() {
        OpenApiConfig config = new OpenApiConfig();
        assertNotNull(config.customOpenAPI());
    }
}