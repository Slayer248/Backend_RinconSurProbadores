package cl.duocuc.probadores.service;

import cl.duocuc.probadores.client.TurnoClient;
import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.dto.TurnoAtendidoResponse;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProbadorService {

    public static final String ESTADO_DESACTIVADO = "DESACTIVADO";

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ProbadorRepository probadorRepository;
    private final TurnoClient turnoClient;

    public List<Probador> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            // Los probadores desactivados se conservan en la BD pero no se listan por defecto
            return probadorRepository.findAllByOrderByCodigoAsc().stream()
                    .filter(p -> !ESTADO_DESACTIVADO.equalsIgnoreCase(p.getEstado()))
                    .toList();
        }
        return probadorRepository.findByEstadoIgnoreCaseOrderByCodigoAsc(estado.trim());
    }

    public Probador obtener(String codigo) {
        return probadorRepository.findById(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Probador no encontrado"));
    }

    public Probador crear(CrearProbadorRequest datos) {
        if (datos.codigo() == null || datos.codigo().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código del probador es inválido");
        }
        String codigoClean = datos.codigo().trim().toUpperCase();
        if (probadorRepository.existsById(codigoClean)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un probador con el código '" + codigoClean + "'");
        }

        Probador nuevo = Probador.builder()
                .codigo(codigoClean)
                .esAccesible(Boolean.TRUE.equals(datos.esAccesible()))
                .estado("LIBRE")
                .desdeHora("-")
                .build();

        return probadorRepository.save(nuevo);
    }

    public Probador cambiarEstado(String codigo, String nuevoEstado) {
        Probador probador = obtener(codigo);
        if (ESTADO_DESACTIVADO.equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El probador está desactivado");
        }
        if ("FUERA_SERVICIO".equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El probador está fuera de servicio");
        }
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        String estadoUpper = nuevoEstado.trim().toUpperCase();

        if ("OCUPADO".equals(estadoUpper)) {
            // Evita consumir otro turno si dos asistentes ocupan el mismo probador a la vez
            if ("OCUPADO".equalsIgnoreCase(probador.getEstado())) {
                return probador;
            }
            TurnoAtendidoResponse turno = turnoClient.atenderSiguiente()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "No hay clientes en la fila de espera para ocupar el probador"
                    ));
            probador.setDesdeHora(LocalTime.now().format(HORA_FMT));
            probador.setUltimoCliente(turno.nombreCliente());
            probador.setUltimoTurno(turno.numeroTurno());
            probador.setUltimoUso(LocalDateTime.now().format(FECHA_HORA_FMT));
        } else if ("LIBRE".equals(estadoUpper)) {
            probador.setDesdeHora("-");
        }

        probador.setEstado(estadoUpper);
        return probadorRepository.save(probador);
    }

    public Probador cambiarBloqueo(String codigo, BloqueoProbadorRequest datos) {
        Probador probador = obtener(codigo);
        if (ESTADO_DESACTIVADO.equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El probador está desactivado");
        }
        boolean bloquear = Boolean.TRUE.equals(datos.bloqueado());

        if (bloquear && "OCUPADO".equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede bloquear " + probador.getCodigo() + " mientras está ocupado");
        }

        if (bloquear) {
            probador.setEstado("FUERA_SERVICIO");
            probador.setDesdeHora("-");
            String motivo = datos.motivo() != null ? datos.motivo().trim() : "";
            probador.setMotivoFueraServicio(motivo.isEmpty() ? null : motivo);
        } else {
            probador.setEstado("LIBRE");
            probador.setDesdeHora("-");
            probador.setMotivoFueraServicio(null);
        }

        return probadorRepository.save(probador);
    }

    public void eliminar(String codigo) {
        Probador probador = obtener(codigo);
        if ("OCUPADO".equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar " + probador.getCodigo() + " mientras está ocupado");
        }
        // Baja lógica: se conservan ultimoCliente, ultimoTurno y ultimoUso.
        probador.setEstado(ESTADO_DESACTIVADO);
        probador.setDesdeHora("-");
        probadorRepository.save(probador);
    }
}