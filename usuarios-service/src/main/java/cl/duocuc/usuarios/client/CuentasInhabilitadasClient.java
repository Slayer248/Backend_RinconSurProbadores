package cl.duocuc.usuarios.client;

import cl.duocuc.usuarios.dto.ArchivarCuentaRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CuentasInhabilitadasClient {

    private final WebClient webClient;

    public CuentasInhabilitadasClient(@Value("${cuentas-inhabilitadas.service.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    /** Guarda la copia de la cuenta. Si falla, lanza excepción para que no se elimine nada. */
    public void archivar(ArchivarCuentaRequest datos) {
        try {
            webClient.post()
                    .uri("/api/cuentas-inhabilitadas")
                    .bodyValue(datos)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo archivar la cuenta: " + e.getStatusCode());
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de cuentas inhabilitadas no está disponible");
        }
    }
}
