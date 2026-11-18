package cl.duocuc.probadores.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnoClientTest {

    @Test
    void atenderSiguienteSiExiste_ServicioNoDisponible_LanzaServiceUnavailable() {
        TurnoClient client = new TurnoClient("http://localhost:59999");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                client::atenderSiguienteSiExiste
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}