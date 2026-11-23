package cl.duocuc.cuentasinhabilitadas.dto;

import cl.duocuc.cuentasinhabilitadas.model.CuentaInhabilitada;

import java.time.LocalDateTime;

/** Vista pública de una cuenta archivada: no incluye la contraseña. */
public record CuentaInhabilitadaResponse(
        Long id,
        Long usuarioId,
        String username,
        String nombre,
        String correo,
        Long rolId,
        String rolNombre,
        Boolean bloqueadaAntes,
        Long inhabilitadaPorId,
        LocalDateTime fechaInhabilitacion
) {
    public static CuentaInhabilitadaResponse desde(CuentaInhabilitada c) {
        return new CuentaInhabilitadaResponse(
                c.getId(), c.getUsuarioId(), c.getUsername(), c.getNombre(), c.getCorreo(),
                c.getRolId(), c.getRolNombre(), c.getBloqueadaAntes(),
                c.getInhabilitadaPorId(), c.getFechaInhabilitacion()
        );
    }
}
