package cl.duocuc.usuarios.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolClientTest {

    @Test
    void metodosRolClient_ServicioCaido_LanzanServiceUnavailable() {
        RolClient client = new RolClient("http://localhost:59999");

        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> client.obtenerPorId(1L));
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, client::obtenerMapaRoles);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex1.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex2.getStatusCode());
    }
}