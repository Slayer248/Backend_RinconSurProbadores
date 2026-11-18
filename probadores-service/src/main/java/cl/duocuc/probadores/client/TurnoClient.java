package cl.duocuc.probadores.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class TurnoClient {

    private final WebClient webClient;

    public TurnoClient(@Value("${turnos.service.url}") String turnosUrl) {
        this.webClient = WebClient.builder().baseUrl(turnosUrl).build();
    }

    public void atenderSiguienteSiExiste() {
        try {
            webClient.post()
                    .uri("/api/turnos/atender-siguiente")
                    .retrieve()
                    .toBodilessEntity()
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de turnos no está disponible");
        }
    }
}