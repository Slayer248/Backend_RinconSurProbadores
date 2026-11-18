package cl.duocuc.turnos.controller;

import cl.duocuc.turnos.dto.CambiarEstadoTurnoRequest;
import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public List<Turno> listar(@RequestParam(required = false, defaultValue = "EN_ESPERA") String estado) {
        return turnoService.listarPorEstado(estado);
    }

    @GetMapping("/{id}")
    public Turno obtener(@PathVariable Long id) {
        return turnoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Turno crear(@RequestBody CrearTurnoRequest datos) {
        return turnoService.crear(datos);
    }

    @PostMapping("/atender-siguiente")
    public Turno atenderSiguiente() {
        return turnoService.atenderSiguiente();
    }

    @PatchMapping("/{id}/estado")
    public Turno cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoTurnoRequest datos) {
        return turnoService.cambiarEstado(id, datos.estado());
    }
}