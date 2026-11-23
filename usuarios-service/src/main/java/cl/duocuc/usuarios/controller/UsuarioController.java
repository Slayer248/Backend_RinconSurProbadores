package cl.duocuc.usuarios.controller;

import cl.duocuc.usuarios.dto.*;
import cl.duocuc.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para autenticación, gestión CRUD, contraseñas y bloqueo de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales del usuario y retorna sus datos de perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuario bloqueado")
    })
    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest datos) {
        return usuarioService.login(datos);
    }

    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios registrados, permitiendo filtrar opcionalmente por texto de búsqueda o ID de rol.")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios obtenido correctamente")
    @GetMapping
    public List<UsuarioResponse> listar(
            @Parameter(description = "Texto para filtrar por nombre, username o correo") @RequestParam(required = false) String buscar,
            @Parameter(description = "ID del rol para filtrar usuarios") @RequestParam(required = false) Long rolId
    ) {
        return usuarioService.listar(buscar, rolId);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Busca y retorna un usuario específico mediante su identificador numérico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @Operation(summary = "Obtener usuario por username", description = "Busca y retorna un usuario mediante su nombre de usuario único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/username/{username}")
    public UsuarioResponse obtenerPorUsername(@Parameter(description = "Nombre de usuario") @PathVariable String username) {
        return usuarioService.obtenerPorUsername(username);
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un usuario en el sistema y envía sus credenciales iniciales por correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario/correo ya existente"),
            @ApiResponse(responseCode = "500", description = "Error al enviar el correo de credenciales")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestBody RegistroUsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody ActualizarUsuarioRequest datos
    ) {
        return usuarioService.actualizar(id, datos);
    }

    @Operation(summary = "Cambiar contraseña", description = "Actualiza la contraseña del usuario validando su clave actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "La contraseña actual no coincide o la nueva es inválida"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody CambiarContrasenaRequest datos
    ) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @Operation(summary = "Recuperar contraseña", description = "Genera una clave temporal y la envía al correo asociado al username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correo de recuperación enviado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.OK)
    public void recuperarContrasena(@RequestBody RecuperarContrasenaRequest datos) {
        usuarioService.recuperarContrasena(datos.username());
    }

    @Operation(summary = "Bloquear o desbloquear usuario", description = "Cambia el estado de bloqueo de una cuenta de usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de bloqueo actualizado"),
            @ApiResponse(responseCode = "400", description = "Operación no permitida (ej. autobloqueo)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/bloqueo")
    public UsuarioResponse cambiarBloqueo(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody BloqueoRequest datos,
            @Parameter(description = "ID del administrador que ejecuta la acción") @RequestHeader(value = "X-Admin-Id", required = false) Long adminId
    ) {
        return usuarioService.cambiarBloqueo(id, datos.bloqueado(), adminId);
    }

    @Operation(summary = "Inhabilitar usuario", description = "Elimina la cuenta de usuarios y guarda una copia permanente en cuentas-inhabilitadas-service. No se puede reactivar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta archivada y eliminada de usuarios"),
            @ApiResponse(responseCode = "503", description = "El servicio de cuentas inhabilitadas no responde; no se eliminó nada"),
            @ApiResponse(responseCode = "400", description = "No se puede inhabilitar al propio administrador"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "ID del administrador que ejecuta la acción") @RequestHeader(value = "X-Admin-Id", required = false) Long adminId
    ) {
        usuarioService.eliminar(id, adminId);
    }
}