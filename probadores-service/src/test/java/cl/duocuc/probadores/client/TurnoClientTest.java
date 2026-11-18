package cl.duocuc.probadores.client;

import cl.duocuc.probadores.dto.TurnoAtendidoResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TurnoClientTest {

    private HttpServer servidorQueResponde(int status, String cuerpo) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/turnos/atender-siguiente", intercambio -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(status, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        return servidor;
    }

    @Test
    void atenderSiguiente_ConClienteEnFila_RetornaSusDatos() throws IOException {
        HttpServer servidor = servidorQueResponde(200,
                "{\"id\":7,\"numeroTurno\":\"T-1001\",\"nombreCliente\":\"Ana Morales\",\"cantidadPrendas\":3,\"esPreferente\":false,\"estado\":\"ATENDIDO\"}");
        try {
            TurnoClient client = new TurnoClient("http://localhost:" + servidor.getAddress().getPort());

            Optional<TurnoAtendidoResponse> turno = client.atenderSiguiente();

            assertTrue(turno.isPresent());
            assertEquals("T-1001", turno.get().numeroTurno());
            assertEquals("Ana Morales", turno.get().nombreCliente());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void atenderSiguiente_FilaVacia_RetornaVacio() throws IOException {
        HttpServer servidor = servidorQueResponde(404, "{\"message\":\"No hay turnos en espera\"}");
        try {
            TurnoClient client = new TurnoClient("http://localhost:" + servidor.getAddress().getPort());

            assertTrue(client.atenderSiguiente().isEmpty());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void atenderSiguiente_ServicioNoDisponible_LanzaServiceUnavailable() {
        TurnoClient client = new TurnoClient("http://localhost:59999");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, client::atenderSiguiente);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}
