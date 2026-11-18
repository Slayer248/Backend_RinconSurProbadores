package cl.duocuc.roles.config;

import cl.duocuc.roles.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_CuandoTablaEstaVacia_InsertaRolesIniciales() {
        when(rolRepository.count()).thenReturn(0L);

        datosIniciales.run();

        verify(rolRepository).saveAll(anyList());
    }

    @Test
    void run_CuandoYaHayDatos_NoInsertaNada() {
        when(rolRepository.count()).thenReturn(2L);

        datosIniciales.run();

        verify(rolRepository, never()).saveAll(anyList());
    }
}