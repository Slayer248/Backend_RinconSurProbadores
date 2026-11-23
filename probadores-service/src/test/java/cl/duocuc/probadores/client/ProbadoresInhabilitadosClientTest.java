package cl.duocuc.probadores.client;

import cl.duocuc.probadores.dto.ArchivarProbadorRequest;
import cl.duocuc.probadores.dto.UsoProbadorDto;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProbadoresInhabilitadosClientTest {

    private HttpServer servidor;

    private final ArchivarProbadorRequest req = new ArchivarProbadorRequest("P-01", false, "LIBRE", null,
            "Ana Morales", "T-1001", "01-01-2026 10:00",
            List.of(new UsoProbadorDto("T-1001", "Ana Morales", LocalDateTime.of(2026, 1, 1, 10, 0), null)));

    @AfterEach
    void detener() {
        if (servidor != null) servidor.stop(0);
    }

    private String iniciarServidor(int status, AtomicReference<String> cuerpoRecibido) throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/probadores-inhabilitados", ex -> {
            cuerpoRecibido.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(status, -1);
            ex.close();
        });
        servidor.start();
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    @Test
    void archivar_RespuestaExitosa_EnviaProbadorYNombresDeInvitados() throws Exception {
        AtomicReference<String> cuerpo = new AtomicReference<>();
        ProbadoresInhabilitadosClient client = new ProbadoresInhabilitadosClient(iniciarServidor(201, cuerpo));

        assertDoesNotThrow(() -> client.archivar(req));

        assertTrue(cuerpo.get().contains("\"codigo\":\"P-01\""));
        assertTrue(cuerpo.get().contains("\"nombreCliente\":\"Ana Morales\""));
    }

    @Test
    void archivar_ServicioRespondeError_LanzaBadGateway() throws Exception {
        ProbadoresInhabilitadosClient client = new ProbadoresInhabilitadosClient(iniciarServidor(500, new AtomicReference<>()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.archivar(req));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void archivar_ServicioCaido_LanzaServiceUnavailable() {
        ProbadoresInhabilitadosClient client = new ProbadoresInhabilitadosClient("http://localhost:59997");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.archivar(req));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}
