package cl.duocuc.usuarios.dto;

import cl.duocuc.usuarios.model.Usuario;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String correo,
        Long rolId,
        String rolNombre,
        Boolean bloqueado
) {
    public static UsuarioResponse desde(Usuario u, String rolNombre) {
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getCorreo(),
                u.getRolId(),
                rolNombre,
                u.getBloqueado()
        );
    }
}