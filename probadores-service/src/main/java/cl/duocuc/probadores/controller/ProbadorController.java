package cl.duocuc.probadores.controller;

import cl.duocuc.probadores.dto.BloqueoProbadorRequest;
import cl.duocuc.probadores.dto.CambiarEstadoProbadorRequest;
import cl.duocuc.probadores.dto.CrearProbadorRequest;
import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.service.ProbadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/probadores")
@RequiredArgsConstructor
public class ProbadorController {

    private final ProbadorService probadorService;

    @GetMapping
    public List<Probador> listar(@RequestParam(required = false) String estado) {
        return probadorService.listar(estado);
    }

    @GetMapping("/{codigo}")
    public Probador obtener(@PathVariable String codigo) {
        return probadorService.obtener(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Probador crear(@RequestBody CrearProbadorRequest datos) {
        return probadorService.crear(datos);
    }

    @PutMapping("/{codigo}/estado")
    public Probador cambiarEstado(@PathVariable String codigo, @RequestBody CambiarEstadoProbadorRequest datos) {
        return probadorService.cambiarEstado(codigo, datos.estado());
    }

    @PatchMapping("/{codigo}/bloqueo")
    public Probador cambiarBloqueo(@PathVariable String codigo, @RequestBody BloqueoProbadorRequest datos) {
        return probadorService.cambiarBloqueo(codigo, datos);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        probadorService.eliminar(codigo);
    }
}