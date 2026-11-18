package cl.duocuc.probadores.service;

import cl.duocuc.probadores.client.TurnoClient;
import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProbadorService {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ProbadorRepository probadorRepository;
    private final TurnoClient turnoClient;

    public List<Probador> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            return probadorRepository.findAllByOrderByCodigoAsc();
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
        if ("FUERA_SERVICIO".equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El probador está fuera de servicio");
        }
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        String estadoUpper = nuevoEstado.trim().toUpperCase();
        probador.setEstado(estadoUpper);

        if ("OCUPADO".equals(estadoUpper)) {
            probador.setDesdeHora(LocalTime.now().format(HORA_FMT));
            turnoClient.atenderSiguienteSiExiste();
        } else if ("LIBRE".equals(estadoUpper)) {
            probador.setDesdeHora("-");
        }

        return probadorRepository.save(probador);
    }

    public Probador cambiarBloqueo(String codigo, BloqueoProbadorRequest datos) {
        Probador probador = obtener(codigo);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar " + probador.getCodigo() + " mientras está ocupado");
        }
        probadorRepository.delete(probador);
    }
}