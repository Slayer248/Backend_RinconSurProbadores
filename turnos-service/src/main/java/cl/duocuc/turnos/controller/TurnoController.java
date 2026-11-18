package cl.duocuc.turnos.controller;

import cl.duocuc.turnos.dto.CambiarEstadoTurnoRequest;
import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.service.TurnoService;
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
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Endpoints para la solicitud, atención y gestión de fila de turnos")
public class TurnoController {

    private final TurnoService turnoService;

    @Operation(summary = "Listar turnos por estado", description = "Obtiene la lista de turnos filtrados por su estado (por defecto retorna los que están EN_ESPERA).")
    @ApiResponse(responseCode = "200", description = "Listado de turnos obtenido correctamente")
    @GetMapping
    public List<Turno> listar(
            @Parameter(description = "Estado del turno (ej. EN_ESPERA, ATENDIDO, CANCELADO)") @RequestParam(required = false, defaultValue = "EN_ESPERA") String estado
    ) {
        return turnoService.listarPorEstado(estado);
    }

    @Operation(summary = "Obtener turno por ID", description = "Busca y retorna un turno específico mediante su identificador numérico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @GetMapping("/{id}")
    public Turno obtener(@Parameter(description = "ID del turno") @PathVariable Long id) {
        return turnoService.obtener(id);
    }

    @Operation(summary = "Solicitar nuevo turno", description = "Registra un nuevo turno en la cola de atención.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para crear el turno")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turno crear(@RequestBody CrearTurnoRequest datos) {
        return turnoService.crear(datos);
    }

    @Operation(summary = "Atender siguiente turno", description = "Toma el próximo turno en espera de la fila y lo pasa a atención.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siguiente turno asignado a atención"),
            @ApiResponse(responseCode = "404", description = "No hay turnos en espera")
    })
    @PostMapping("/atender-siguiente")
    public Turno atenderSiguiente() {
        return turnoService.atenderSiguiente();
    }

    @Operation(summary = "Cambiar estado del turno", description = "Actualiza manualmente el estado de un turno existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del turno actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public Turno cambiarEstado(
            @Parameter(description = "ID del turno") @PathVariable Long id,
            @RequestBody CambiarEstadoTurnoRequest datos
    ) {
        return turnoService.cambiarEstado(id, datos.estado());
    }
}