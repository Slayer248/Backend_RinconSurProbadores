package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_TablaVacia_CreaAdminPorDefecto() {
        when(usuarioRepository.count()).thenReturn(0L);

        datosIniciales.run();

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void run_TablaConDatos_NoCreaNada() {
        when(usuarioRepository.count()).thenReturn(1L);

        datosIniciales.run();

        verify(usuarioRepository, never()).save(any());
    }
}