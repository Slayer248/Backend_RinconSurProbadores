package cl.duocuc.cuentasinhabilitadas.service;

import cl.duocuc.cuentasinhabilitadas.dto.ArchivarCuentaRequest;
import cl.duocuc.cuentasinhabilitadas.dto.CuentaInhabilitadaResponse;
import cl.duocuc.cuentasinhabilitadas.model.CuentaInhabilitada;
import cl.duocuc.cuentasinhabilitadas.repository.CuentaInhabilitadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaInhabilitadaService {

    private final CuentaInhabilitadaRepository repository;

    /** Idempotente: si la cuenta ya estaba archivada, devuelve el registro existente. */
    public CuentaInhabilitadaResponse archivar(ArchivarCuentaRequest d) {
        if (d.usuarioId() == null || vacio(d.username()) || vacio(d.nombre())
                || vacio(d.correo()) || vacio(d.contrasena()) || d.rolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos de la cuenta a archivar");
        }

        return repository.findByUsuarioId(d.usuarioId())
                .map(CuentaInhabilitadaResponse::desde)
                .orElseGet(() -> CuentaInhabilitadaResponse.desde(repository.save(
                        CuentaInhabilitada.builder()
                                .usuarioId(d.usuarioId())
                                .username(d.username())
                                .nombre(d.nombre())
                                .correo(d.correo())
                                .contrasena(d.contrasena())
                                .rolId(d.rolId())
                                .rolNombre(d.rolNombre())
                                .bloqueadaAntes(Boolean.TRUE.equals(d.bloqueadaAntes()))
                                .inhabilitadaPorId(d.inhabilitadaPorId())
                                .fechaInhabilitacion(LocalDateTime.now())
                                .build())));
    }

    public List<CuentaInhabilitadaResponse> listar() {
        return repository.findAllByOrderByFechaInhabilitacionDesc().stream()
                .map(CuentaInhabilitadaResponse::desde)
                .toList();
    }

    public CuentaInhabilitadaResponse obtener(Long id) {
        return repository.findById(id)
                .map(CuentaInhabilitadaResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta inhabilitada no encontrada"));
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
