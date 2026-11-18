package cl.duocuc.turnos.service;

import cl.duocuc.turnos.dto.CrearTurnoRequest;
import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public List<Turno> listarPorEstado(String estado) {
        String filtro = (estado == null || estado.isBlank()) ? "EN_ESPERA" : estado.trim().toUpperCase();
        return turnoRepository.findByEstadoOrderByEsPreferenteDescIdAsc(filtro);
    }

    public Turno obtener(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
    }

    public Turno crear(CrearTurnoRequest datos) {
        if (datos.nombreCliente() == null || datos.nombreCliente().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente es obligatorio");
        }
        if (datos.cantidadPrendas() == null || datos.cantidadPrendas() < 1 || datos.cantidadPrendas() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad de prendas debe ser entre 1 y 6");
        }

        String nombreClean = datos.nombreCliente().trim();
        if (turnoRepository.existsByNombreClienteIgnoreCaseAndEstado(nombreClean, "EN_ESPERA")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes un turno activo en espera. Debes ser atendido antes de solicitar otro.");
        }

        long correlativo = 1001L + turnoRepository.count();
        Turno nuevo = Turno.builder()
                .numeroTurno("T-" + correlativo)
                .nombreCliente(nombreClean)
                .cantidadPrendas(datos.cantidadPrendas())
                .esPreferente(Boolean.TRUE.equals(datos.esPreferente()))
                .estado("EN_ESPERA")
                .build();

        return turnoRepository.save(nuevo);
    }

    public Turno atenderSiguiente() {
        Turno siguiente = turnoRepository.findFirstByEstadoOrderByEsPreferenteDescIdAsc("EN_ESPERA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay turnos en espera"));
        siguiente.setEstado("ATENDIDO");
        return turnoRepository.save(siguiente);
    }

    public Turno cambiarEstado(Long id, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        Turno turno = obtener(id);
        turno.setEstado(nuevoEstado.trim().toUpperCase());
        return turnoRepository.save(turno);
    }
}