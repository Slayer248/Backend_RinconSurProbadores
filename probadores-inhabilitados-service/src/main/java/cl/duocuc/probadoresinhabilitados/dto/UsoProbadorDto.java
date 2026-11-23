package cl.duocuc.probadoresinhabilitados.dto;

import java.time.LocalDateTime;

public record UsoProbadorDto(
        String numeroTurno,
        String nombreCliente,
        LocalDateTime inicio,
        LocalDateTime fin
) {
}
