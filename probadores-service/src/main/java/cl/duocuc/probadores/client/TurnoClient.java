package cl.duocuc.probadores.client;

import cl.duocuc.probadores.dto.TurnoAtendidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class TurnoClient {

    private final WebClient webClient;

    public TurnoClient(@Value("${turnos.service.url}") String turnosUrl) {
        this.webClient = WebClient.builder().baseUrl(turnosUrl).build();
    }

    /** Atiende al siguiente cliente de la fila; vacío si no hay nadie esperando. */
    public Optional<TurnoAtendidoResponse> atenderSiguiente() {
        try {
            return webClient.post()
                    .uri("/api/turnos/atender-siguiente")
                    .retrieve()
                    .bodyToMono(TurnoAtendidoResponse.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .blockOptional();
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de turnos no está disponible");
        }
    }
}