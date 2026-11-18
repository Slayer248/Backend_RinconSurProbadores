package cl.duocuc.roles.controller;

import cl.duocuc.roles.model.Rol;
import cl.duocuc.roles.service.RolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {

    @Mock
    private RolService rolService;

    @InjectMocks
    private RolController rolController;

    @Test
    void listar_DelegaAlServicio() {
        when(rolService.listar()).thenReturn(List.of(Rol.builder().id(1L).nombre("ADMIN").build()));

        List<Rol> resultado = rolController.listar();

        assertEquals(1, resultado.size());
        verify(rolService).listar();
    }

    @Test
    void obtener_DelegaAlServicio() {
        Rol rol = Rol.builder().id(1L).nombre("ADMIN").build();
        when(rolService.obtener(1L)).thenReturn(rol);

        Rol resultado = rolController.obtener(1L);

        assertEquals("ADMIN", resultado.getNombre());
        verify(rolService).obtener(1L);
    }

    @Test
    void crear_DelegaAlServicio() {
        Rol entrada = Rol.builder().nombre("SUPERVISOR").build();
        Rol creado = Rol.builder().id(3L).nombre("SUPERVISOR").build();
        when(rolService.crear(entrada)).thenReturn(creado);

        Rol resultado = rolController.crear(entrada);

        assertEquals(3L, resultado.getId());
        verify(rolService).crear(entrada);
    }
}