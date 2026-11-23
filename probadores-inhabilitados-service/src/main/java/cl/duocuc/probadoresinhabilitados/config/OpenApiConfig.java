package cl.duocuc.probadoresinhabilitados.config;

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
                        .title("API Microservicio de Probadores Inhabilitados - Rincón Sur")
                        .version("1.0")
                        .description("Archivo histórico de los probadores eliminados, con las personas que los usaron."));
    }
}
