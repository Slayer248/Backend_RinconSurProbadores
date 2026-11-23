package cl.duocuc.probadoresinhabilitados.service;

import cl.duocuc.probadoresinhabilitados.dto.ArchivarProbadorRequest;
import cl.duocuc.probadoresinhabilitados.dto.ProbadorInhabilitadoResponse;
import cl.duocuc.probadoresinhabilitados.dto.UsoProbadorDto;
import cl.duocuc.probadoresinhabilitados.model.ProbadorInhabilitado;
import cl.duocuc.probadoresinhabilitados.model.UsoProbadorArchivado;
import cl.duocuc.probadoresinhabilitados.repository.ProbadorInhabilitadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProbadorInhabilitadoService {

    private final ProbadorInhabilitadoRepository repository;

    /** Guarda el probador y todos sus usos en una sola transacción. */
    @Transactional
    public ProbadorInhabilitadoResponse archivar(ArchivarProbadorRequest d) {
        if (d.codigo() == null || d.codigo().isBlank() || d.esAccesible() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del probador a archivar");
        }

        ProbadorInhabilitado archivo = ProbadorInhabilitado.builder()
                .codigo(d.codigo())
                .esAccesible(d.esAccesible())
                .estadoAnterior(d.estadoAnterior())
                .motivoFueraServicio(d.motivoFueraServicio())
                .ultimoCliente(d.ultimoCliente())
                .ultimoTurno(d.ultimoTurno())
                .ultimoUso(d.ultimoUso())
                .fechaInhabilitacion(LocalDateTime.now())
                .build();

        List<UsoProbadorDto> usos = d.usos() == null ? List.of() : d.usos();
        for (UsoProbadorDto u : usos) {
            if (u.nombreCliente() == null || u.nombreCliente().isBlank() || u.inicio() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay usos del probador con datos incompletos");
            }
            archivo.getUsos().add(UsoProbadorArchivado.builder()
                    .probador(archivo)
                    .codigoProbador(d.codigo())
                    .numeroTurno(u.numeroTurno())
                    .nombreCliente(u.nombreCliente())
                    .inicio(u.inicio())
                    .fin(u.fin())
                    .build());
        }

        return ProbadorInhabilitadoResponse.desde(repository.save(archivo));
    }

    @Transactional(readOnly = true)
    public List<ProbadorInhabilitadoResponse> listar() {
        return repository.findAllByOrderByFechaInhabilitacionDesc().stream()
                .map(ProbadorInhabilitadoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProbadorInhabilitadoResponse obtener(Long id) {
        return repository.findById(id)
                .map(ProbadorInhabilitadoResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Probador inhabilitado no encontrado"));
    }
}
