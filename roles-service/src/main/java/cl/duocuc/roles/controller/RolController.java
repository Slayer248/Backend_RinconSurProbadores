package cl.duocuc.roles.controller;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.service.RolService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Endpoints para la consulta y creación de roles del sistema")
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar roles", description = "Obtiene el listado completo de roles registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Listado de roles obtenido correctamente")
    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @Operation(summary = "Obtener rol por ID", description = "Busca y retorna un rol específico mediante su identificador numérico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol encontrado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @GetMapping("/{id}")
    public Rol obtener(@Parameter(description = "ID del rol") @PathVariable Long id) {
        return rolService.obtener(id);
    }

    @Operation(summary = "Crear nuevo rol", description = "Registra un nuevo rol en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre de rol duplicado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rol crear(@RequestBody Rol rol) {
        return rolService.crear(rol);
    }
}