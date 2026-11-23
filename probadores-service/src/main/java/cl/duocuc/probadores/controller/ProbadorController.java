package cl.duocuc.probadores.controller;

import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CambiarEstadoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.dto.UsoProbadorDto;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.service.ProbadorService;
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
@RequestMapping("/api/probadores")
@RequiredArgsConstructor
@Tag(name = "Probadores", description = "Endpoints para la gestión, disponibilidad y bloqueo de probadores")
public class ProbadorController {

    private final ProbadorService probadorService;

    @Operation(summary = "Listar probadores", description = "Obtiene todos los probadores registrados, permitiendo filtrar opcionalmente por su estado.")
    @ApiResponse(responseCode = "200", description = "Listado de probadores obtenido correctamente")
    @GetMapping
    public List<Probador> listar(
            @Parameter(description = "Estado del probador para filtrar (ej. DISPONIBLE, OCUPADO)") @RequestParam(required = false) String estado
    ) {
        return probadorService.listar(estado);
    }

    @Operation(summary = "Obtener probador por código", description = "Busca y retorna un probador específico mediante su código único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Probador encontrado"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @GetMapping("/{codigo}")
    public Probador obtener(@Parameter(description = "Código único del probador") @PathVariable String codigo) {
        return probadorService.obtener(codigo);
    }

    @Operation(summary = "Crear nuevo probador", description = "Registra un nuevo probador en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Probador creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o código de probador duplicado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Probador crear(@RequestBody CrearProbadorRequest datos) {
        return probadorService.crear(datos);
    }

    @Operation(summary = "Cambiar estado del probador", description = "Actualiza el estado operativo de un probador (ej. disponible u ocupado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @PutMapping("/{codigo}/estado")
    public Probador cambiarEstado(
            @Parameter(description = "Código único del probador") @PathVariable String codigo,
            @RequestBody CambiarEstadoProbadorRequest datos
    ) {
        return probadorService.cambiarEstado(codigo, datos.estado());
    }

    @Operation(summary = "Bloquear o desbloquear probador", description = "Habilita o deshabilita un probador por mantención o indisponibilidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloqueo del probador actualizado"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @PatchMapping("/{codigo}/bloqueo")
    public Probador cambiarBloqueo(
            @Parameter(description = "Código único del probador") @PathVariable String codigo,
            @RequestBody BloqueoProbadorRequest datos
    ) {
        return probadorService.cambiarBloqueo(codigo, datos);
    }

    @Operation(summary = "Historial de uso del probador", description = "Lista las personas (nombre del invitado y turno) que han usado el probador, con hora de inicio y fin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @GetMapping("/{codigo}/historial")
    public List<UsoProbadorDto> historial(@Parameter(description = "Código único del probador") @PathVariable String codigo) {
        return probadorService.historial(codigo);
    }

    @Operation(summary = "Eliminar probador", description = "Elimina el probador de probadores y guarda una copia permanente, con todas las personas que lo usaron, en probadores-inhabilitados-service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Probador archivado y eliminado"),
            @ApiResponse(responseCode = "409", description = "El probador está ocupado"),
            @ApiResponse(responseCode = "503", description = "El servicio de probadores inhabilitados no responde; no se eliminó nada"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "Código único del probador") @PathVariable String codigo) {
        probadorService.eliminar(codigo);
    }
}