package cl.duocuc.probadores.service;

import cl.duocuc.probadores.client.ProbadoresInhabilitadosClient;
import cl.duocuc.probadores.client.TurnoClient;
import cl.duocuc.probadores.dto.ArchivarProbadorRequest;
import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.dto.TurnoAtendidoResponse;
import cl.duocuc.probadores.dto.UsoProbadorDto;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.model.UsoProbador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import cl.duocuc.probadores.repository.UsoProbadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UsoProbadorRepository usoProbadorRepository;
    private final ProbadoresInhabilitadosClient probadoresInhabilitadosClient;

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

    @Transactional
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
            LocalDateTime ahora = LocalDateTime.now();
            probador.setDesdeHora(ahora.format(HORA_FMT));
            probador.setUltimoCliente(turno.nombreCliente());
            probador.setUltimoTurno(turno.numeroTurno());
            probador.setUltimoUso(ahora.format(FECHA_HORA_FMT));
            // Historial: queda una fila por cada persona que usa el probador
            usoProbadorRepository.save(UsoProbador.builder()
                    .codigoProbador(probador.getCodigo())
                    .numeroTurno(turno.numeroTurno())
                    .nombreCliente(turno.nombreCliente())
                    .inicio(ahora)
                    .build());
        } else if ("LIBRE".equals(estadoUpper)) {
            probador.setDesdeHora("-");
            usoProbadorRepository.findFirstByCodigoProbadorAndFinIsNullOrderByInicioDesc(probador.getCodigo())
                    .ifPresent(uso -> {
                        uso.setFin(LocalDateTime.now());
                        usoProbadorRepository.save(uso);
                    });
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

    @Transactional
    public void eliminar(String codigo) {
        Probador probador = obtener(codigo);
        if ("OCUPADO".equalsIgnoreCase(probador.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar " + probador.getCodigo() + " mientras está ocupado");
        }

        // Primero se archiva el probador con todas las personas que lo usaron; si falla, no se elimina nada.
        List<UsoProbadorDto> usos = usoProbadorRepository.findByCodigoProbadorOrderByInicioAsc(probador.getCodigo())
                .stream()
                .map(u -> new UsoProbadorDto(u.getNumeroTurno(), u.getNombreCliente(), u.getInicio(), u.getFin()))
                .toList();
        probadoresInhabilitadosClient.archivar(new ArchivarProbadorRequest(
                probador.getCodigo(),
                probador.getEsAccesible(),
                probador.getEstado(),
                probador.getMotivoFueraServicio(),
                probador.getUltimoCliente(),
                probador.getUltimoTurno(),
                probador.getUltimoUso(),
                usos
        ));

        // Ya archivado, desaparece de probadores y de su historial de uso.
        usoProbadorRepository.deleteByCodigoProbador(probador.getCodigo());
        probadorRepository.delete(probador);
    }

    /** Todas las personas que han usado el probador, de la más antigua a la más reciente. */
    public List<UsoProbadorDto> historial(String codigo) {
        Probador probador = obtener(codigo);
        return usoProbadorRepository.findByCodigoProbadorOrderByInicioAsc(probador.getCodigo()).stream()
                .map(u -> new UsoProbadorDto(u.getNumeroTurno(), u.getNombreCliente(), u.getInicio(), u.getFin()))
                .toList();
    }
}