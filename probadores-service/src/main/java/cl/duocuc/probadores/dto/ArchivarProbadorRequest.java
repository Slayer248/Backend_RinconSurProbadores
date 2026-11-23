package cl.duocuc.probadores.dto;

import java.util.List;

/** Datos que se envían a probadores-inhabilitados-service al eliminar un probador. */
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
