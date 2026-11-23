package cl.duocuc.probadoresinhabilitados.controller;

import cl.duocuc.probadoresinhabilitados.dto.ArchivarProbadorRequest;
import cl.duocuc.probadoresinhabilitados.dto.ProbadorInhabilitadoResponse;
import cl.duocuc.probadoresinhabilitados.service.ProbadorInhabilitadoService;
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
@RequestMapping("/api/probadores-inhabilitados")
@RequiredArgsConstructor
@Tag(name = "Probadores inhabilitados", description = "Archivo histórico de probadores eliminados y de las personas que los usaron. Solo se agregan y consultan.")
public class ProbadorInhabilitadoController {

    private final ProbadorInhabilitadoService service;

    @Operation(summary = "Archivar probador", description = "Guarda una copia del probador eliminado junto con todos sus usos (nombre del invitado, turno y horas).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Probador archivado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos del probador o de sus usos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProbadorInhabilitadoResponse archivar(@RequestBody ArchivarProbadorRequest datos) {
        return service.archivar(datos);
    }

    @Operation(summary = "Listar probadores inhabilitados", description = "Retorna los probadores archivados con sus usos, los más recientes primero.")
    @GetMapping
    public List<ProbadorInhabilitadoResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Obtener probador inhabilitado por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Probador encontrado"),
            @ApiResponse(responseCode = "404", description = "Probador no encontrado")
    })
    @GetMapping("/{id}")
    public ProbadorInhabilitadoResponse obtener(@Parameter(description = "ID del registro archivado") @PathVariable Long id) {
        return service.obtener(id);
    }
}
