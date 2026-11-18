package cl.duocuc.roles.service;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
    void listar_RetornaRolesOrdenados() {
        List<Rol> lista = List.of(
                Rol.builder().id(1L).nombre("ADMIN").build(),
                Rol.builder().id(2L).nombre("ASISTENTE").build()
        );
        when(rolRepository.findAllByOrderByIdAsc()).thenReturn(lista);

        List<Rol> resultado = rolService.listar();

        assertEquals(2, resultado.size());
        assertEquals("ADMIN", resultado.get(0).getNombre());
        verify(rolRepository).findAllByOrderByIdAsc();
    }

    @Test
    void obtener_ExitosoRetornaRol() {
        Rol rol = Rol.builder().id(1L).nombre("ADMIN").build();
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));

        Rol resultado = rolService.obtener(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("ADMIN", resultado.getNombre());
    }

    @Test
    void obtener_NoExisteLanzaNotFound() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                rolService.obtener(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

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

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                rolService.crear(Rol.builder().nombre("admin").build())
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(rolRepository, never()).save(any());
    }

    @Test
    void crearRol_NombreNuloOVacioLanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> rolService.crear(Rol.builder().nombre(null).build()));
        assertThrows(ResponseStatusException.class, () -> rolService.crear(Rol.builder().nombre("   ").build()));
        verify(rolRepository, never()).save(any());
    }
}