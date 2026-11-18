package cl.duocuc.turnos.config;

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
                        .title("API Microservicio de Turnos - Rincón Sur")
                        .version("1.0")
                        .description("Documentación de endpoints para la reserva, consulta y administración de turnos."));
    }
}