package cl.duocuc.usuarios.dto;

/** Datos que se envían a cuentas-inhabilitadas-service al eliminar una cuenta. */
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
