package cl.duocuc.usuarios.dto;

public record RegistroUsuarioRequest(
        String username,
        String nombre,
        String correo,
        String contrasena,
        Long rolId
) {}