package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolClient rolClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioResponse login(LoginRequest datos) {
        if (datos.username() == null || datos.username().isBlank() ||
            datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario y contraseña obligatorios");
        }

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(datos.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(datos.contrasena().trim(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }

        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta se encuentra bloqueada. Contacta al administrador.");
        }

        RolDto rol = rolClient.obtenerPorId(usuario.getRolId());
        return UsuarioResponse.desde(usuario, rol.nombre());
    }

    public List<UsuarioResponse> listar(String buscar, Long rolId) {
        Map<Long, String> mapaRoles = rolClient.obtenerMapaRoles();
        String filtro = buscar != null ? buscar.trim().toLowerCase() : "";

        return usuarioRepository.findAllByOrderByIdAsc().stream()
                .filter(u -> rolId == null || u.getRolId().equals(rolId))
                .filter(u -> filtro.isEmpty()
                        || u.getUsername().toLowerCase().contains(filtro)
                        || u.getNombre().toLowerCase().contains(filtro)
                        || u.getCorreo().toLowerCase().contains(filtro))
                .map(u -> UsuarioResponse.desde(u, mapaRoles.getOrDefault(u.getRolId(), "DESCONOCIDO")))
                .toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        RolDto rol = rolClient.obtenerPorId(u.getRolId());
        return UsuarioResponse.desde(u, rol.nombre());
    }

    public UsuarioResponse obtenerPorUsername(String username) {
        Usuario u = usuarioRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        RolDto rol = rolClient.obtenerPorId(u.getRolId());
        return UsuarioResponse.desde(u, rol.nombre());
    }

    public UsuarioResponse crear(RegistroUsuarioRequest datos) {
        validarCamposBase(datos.username(), datos.nombre(), datos.correo(), datos.rolId());
        validarContrasenaFuerte(datos.contrasena());

        String userClean = datos.username().trim();
        String mailClean = datos.correo().trim();

        if (usuarioRepository.existsByUsernameIgnoreCase(userClean)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario '" + userClean + "' ya existe");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(mailClean)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo '" + mailClean + "' ya está registrado");
        }

        RolDto rol = rolClient.obtenerPorId(datos.rolId());

        Usuario nuevo = Usuario.builder()
                .username(userClean)
                .nombre(datos.nombre().trim())
                .correo(mailClean)
                .contrasena(passwordEncoder.encode(datos.contrasena().trim()))
                .rolId(rol.id())
                .bloqueado(false)
                .build();

        return UsuarioResponse.desde(usuarioRepository.save(nuevo), rol.nombre());
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest datos) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarCamposBase(datos.username(), datos.nombre(), datos.correo(), datos.rolId());

        String userClean = datos.username().trim();
        String mailClean = datos.correo().trim();

        if ("admin".equalsIgnoreCase(existente.getUsername()) && !"admin".equalsIgnoreCase(userClean)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede cambiar el username del administrador principal");
        }
        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(userClean, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario '" + userClean + "' ya está en uso");
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(mailClean, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo '" + mailClean + "' ya está en uso");
        }

        RolDto rol = rolClient.obtenerPorId(datos.rolId());
        if ("admin".equalsIgnoreCase(existente.getUsername()) && !"ADMIN".equalsIgnoreCase(rol.nombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede quitar el rol ADMIN al administrador principal");
        }

        existente.setUsername(userClean);
        existente.setNombre(datos.nombre().trim());
        existente.setCorreo(mailClean);
        existente.setRolId(rol.id());

        if (datos.nuevaContrasena() != null && !datos.nuevaContrasena().isBlank()) {
            validarContrasenaFuerte(datos.nuevaContrasena());
            existente.setContrasena(passwordEncoder.encode(datos.nuevaContrasena().trim()));
        }

        return UsuarioResponse.desde(usuarioRepository.save(existente), rol.nombre());
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (datos.actual() == null || !passwordEncoder.matches(datos.actual().trim(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }

        validarContrasenaFuerte(datos.nueva());
        usuario.setContrasena(passwordEncoder.encode(datos.nueva().trim()));
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse cambiarBloqueo(Long id, Boolean bloqueado, Long adminId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede bloquear al administrador principal");
        }
        if (adminId != null && adminId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes bloquear tu propia cuenta");
        }

        usuario.setBloqueado(Boolean.TRUE.equals(bloqueado));
        RolDto rol = rolClient.obtenerPorId(usuario.getRolId());
        return UsuarioResponse.desde(usuarioRepository.save(usuario), rol.nombre());
    }

    public void eliminar(Long id, Long adminId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede eliminar al administrador principal");
        }
        if (adminId != null && adminId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar tu propia cuenta");
        }

        usuarioRepository.delete(usuario);
    }

    private void validarCamposBase(String username, String nombre, String correo, Long rolId) {
        if (username == null || username.trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario debe tener al menos 3 caracteres");
        }
        if (nombre == null || nombre.trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre debe tener al menos 3 caracteres");
        }
        if (correo == null || !correo.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo válido");
        }
        if (rolId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }
    }

    private void validarContrasenaFuerte(String pass) {
        if (pass == null || pass.trim().length() < 8
                || !pass.matches(".*[A-Za-z].*")
                || !pass.matches(".*\\d.*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres, una letra y un número"
            );
        }
    }
}