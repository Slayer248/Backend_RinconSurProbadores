package cl.duocuc.cuentasinhabilitadas.dto;

public record ArchivarCuentaRequest(
        Long usuarioId,
        String username,
        String nombre,
        String correo,
        String contrasena,
        Long rolId,
        String rolNombre,
        Boolean bloqueadaAntes,
        Long inhabilitadaPorId
) {
}
