package cl.duocuc.cuentasinhabilitadas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Microservicio de Cuentas Inhabilitadas - Rincón Sur")
                        .version("1.0")
                        .description("Archivo histórico de las cuentas eliminadas desde el panel de administración."));
    }
}
