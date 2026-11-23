package cl.duocuc.usuarios.client;

import cl.duocuc.usuarios.dto.ArchivarCuentaRequest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CuentasInhabilitadasClientTest {

    private HttpServer servidor;

    private final ArchivarCuentaRequest req = new ArchivarCuentaRequest(5L, "mgonzalez", "Maria Gonzalez",
            "mg@rinconsur.test", "hash", 2L, "ASISTENTE", false, 1L);

    @AfterEach
    void detener() {
        if (servidor != null) servidor.stop(0);
    }

    private String iniciarServidor(int status, AtomicReference<String> cuerpoRecibido) throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/cuentas-inhabilitadas", ex -> {
            cuerpoRecibido.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(status, -1);
            ex.close();
        });
        servidor.start();
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    @Test
    void archivar_RespuestaExitosa_EnviaLosDatosDeLaCuenta() throws Exception {
        AtomicReference<String> cuerpo = new AtomicReference<>();
        CuentasInhabilitadasClient client = new CuentasInhabilitadasClient(iniciarServidor(201, cuerpo));

        assertDoesNotThrow(() -> client.archivar(req));

        assertTrue(cuerpo.get().contains("\"username\":\"mgonzalez\""));
        assertTrue(cuerpo.get().contains("\"usuarioId\":5"));
    }

    @Test
    void archivar_ServicioRespondeError_LanzaBadGateway() throws Exception {
        CuentasInhabilitadasClient client = new CuentasInhabilitadasClient(iniciarServidor(500, new AtomicReference<>()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.archivar(req));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void archivar_ServicioCaido_LanzaServiceUnavailable() {
        CuentasInhabilitadasClient client = new CuentasInhabilitadasClient("http://localhost:59998");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.archivar(req));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}
