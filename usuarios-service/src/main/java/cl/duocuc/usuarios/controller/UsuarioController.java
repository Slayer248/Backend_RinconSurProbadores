package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest datos) {
        return usuarioService.login(datos);
    }

    @GetMapping
    public List<UsuarioResponse> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long rolId
    ) {
        return usuarioService.listar(buscar, rolId);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @GetMapping("/username/{username}")
    public UsuarioResponse obtenerPorUsername(@PathVariable String username) {
        return usuarioService.obtenerPorUsername(username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestBody RegistroUsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest datos) {
        return usuarioService.actualizar(id, datos);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.OK)
    public void recuperarContrasena(@RequestBody RecuperarContrasenaRequest datos) {
        usuarioService.recuperarContrasena(datos.username());
    }

    @PatchMapping("/{id}/bloqueo")
    public UsuarioResponse cambiarBloqueo(
            @PathVariable Long id,
            @RequestBody BloqueoRequest datos,
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId
    ) {
        return usuarioService.cambiarBloqueo(id, datos.bloqueado(), adminId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId
    ) {
        usuarioService.eliminar(id, adminId);
    }
}