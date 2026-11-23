package cl.duocuc.probadoresinhabilitados.dto;

import cl.duocuc.probadoresinhabilitados.model.ProbadorInhabilitado;

import java.time.LocalDateTime;
import java.util.List;

public record ProbadorInhabilitadoResponse(
        Long id,
        String codigo,
        Boolean esAccesible,
        String estadoAnterior,
        String motivoFueraServicio,
        String ultimoCliente,
        String ultimoTurno,
        String ultimoUso,
        LocalDateTime fechaInhabilitacion,
        List<UsoProbadorDto> usos
) {
    public static ProbadorInhabilitadoResponse desde(ProbadorInhabilitado p) {
        return new ProbadorInhabilitadoResponse(
                p.getId(), p.getCodigo(), p.getEsAccesible(), p.getEstadoAnterior(),
                p.getMotivoFueraServicio(), p.getUltimoCliente(), p.getUltimoTurno(), p.getUltimoUso(),
                p.getFechaInhabilitacion(),
                p.getUsos().stream()
                        .map(u -> new UsoProbadorDto(u.getNumeroTurno(), u.getNombreCliente(), u.getInicio(), u.getFin()))
                        .toList()
        );
    }
}
