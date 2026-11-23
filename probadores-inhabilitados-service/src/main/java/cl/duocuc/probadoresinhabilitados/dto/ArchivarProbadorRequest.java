package cl.duocuc.probadoresinhabilitados.dto;

import java.util.List;

public record ArchivarProbadorRequest(
        String codigo,
        Boolean esAccesible,
        String estadoAnterior,
        String motivoFueraServicio,
        String ultimoCliente,
        String ultimoTurno,
        String ultimoUso,
        List<UsoProbadorDto> usos
) {
}
