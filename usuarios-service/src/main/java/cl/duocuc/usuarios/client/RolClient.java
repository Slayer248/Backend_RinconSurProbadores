package cl.duocuc.usuarios.client;

import cl.duocuc.usuarios.dto.RolDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RolClient {

    private final WebClient webClient;

    public RolClient(@Value("${roles.service.url}") String rolesUrl) {
        this.webClient = WebClient.builder().baseUrl(rolesUrl).build();
    }

    public RolDto obtenerPorId(Long rolId) {
        try {
            return webClient.get()
                    .uri("/api/roles/{id}", rolId)
                    .retrieve()
                    .bodyToMono(RolDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol seleccionado no existe");
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
        }
    }

    public Map<Long, String> obtenerMapaRoles() {
        try {
            List<RolDto> roles = webClient.get()
                    .uri("/api/roles")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<RolDto>>() {})
                    .block();
            if (roles == null) return Map.of();
            return roles.stream().collect(Collectors.toMap(RolDto::id, RolDto::nombre));
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
        }
    }
}