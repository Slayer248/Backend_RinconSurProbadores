package cl.duocuc.roles.service;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    @Test
    void crearRol_ExitosoNormalizaAMayusculas() {
        when(rolRepository.existsByNombreIgnoreCase("SUPERVISOR")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenReturn(Rol.builder().id(3L).nombre("SUPERVISOR").build());

        Rol resultado = rolService.crear(Rol.builder().nombre("  supervisor ").build());

        assertEquals("SUPERVISOR", resultado.getNombre());
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    void crearRol_DuplicadoLanzaConflicto() {
        when(rolRepository.existsByNombreIgnoreCase("ADMIN")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                rolService.crear(Rol.builder().nombre("admin").build())
        );
        verify(rolRepository, never()).save(any());
    }
}