package cl.duocuc.probadores.client;

import cl.duocuc.probadores.dto.ArchivarProbadorRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProbadoresInhabilitadosClient {

    private final WebClient webClient;

    public ProbadoresInhabilitadosClient(@Value("${probadores-inhabilitados.service.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    /** Guarda la copia del probador. Si falla, lanza excepción para que no se elimine nada. */
    public void archivar(ArchivarProbadorRequest datos) {
        try {
            webClient.post()
                    .uri("/api/probadores-inhabilitados")
                    .bodyValue(datos)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo archivar el probador: " + e.getStatusCode());
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de probadores inhabilitados no está disponible");
        }
    }
}
