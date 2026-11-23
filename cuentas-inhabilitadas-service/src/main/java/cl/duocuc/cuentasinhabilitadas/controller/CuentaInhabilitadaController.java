package cl.duocuc.cuentasinhabilitadas.controller;

import cl.duocuc.cuentasinhabilitadas.dto.ArchivarCuentaRequest;
import cl.duocuc.cuentasinhabilitadas.dto.CuentaInhabilitadaResponse;
import cl.duocuc.cuentasinhabilitadas.service.CuentaInhabilitadaService;
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
@RequestMapping("/api/cuentas-inhabilitadas")
@RequiredArgsConstructor
@Tag(name = "Cuentas inhabilitadas", description = "Archivo histórico de cuentas eliminadas. Solo se agregan y consultan; no se borran ni se reactivan.")
public class CuentaInhabilitadaController {

    private final CuentaInhabilitadaService service;

    @Operation(summary = "Archivar cuenta", description = "Guarda una copia de la cuenta eliminada. Si ya estaba archivada, devuelve el registro existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta archivada"),
            @ApiResponse(responseCode = "400", description = "Faltan datos de la cuenta")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaInhabilitadaResponse archivar(@RequestBody ArchivarCuentaRequest datos) {
        return service.archivar(datos);
    }

    @Operation(summary = "Listar cuentas inhabilitadas", description = "Retorna las cuentas archivadas, las más recientes primero. No incluye contraseñas.")
    @GetMapping
    public List<CuentaInhabilitadaResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Obtener cuenta inhabilitada por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/{id}")
    public CuentaInhabilitadaResponse obtener(@Parameter(description = "ID del registro archivado") @PathVariable Long id) {
        return service.obtener(id);
    }
}
