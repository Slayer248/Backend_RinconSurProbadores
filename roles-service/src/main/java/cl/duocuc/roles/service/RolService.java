package cl.duocuc.roles.service;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> listar() {
        return rolRepository.findAllByOrderByIdAsc();
    }

    public Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    public Rol crear(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del rol es obligatorio");
        }
        String nombreLimpio = rol.getNombre().trim().toUpperCase();
        if (rolRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol '" + nombreLimpio + "' ya existe");
        }
        return rolRepository.save(Rol.builder().nombre(nombreLimpio).build());
    }
}