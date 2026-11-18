package cl.duocuc.usuarios.dto;

public record ActualizarUsuarioRequest(
        String username,
        String nombre,
        String correo,
        Long rolId,
        String nuevaContrasena
) {}