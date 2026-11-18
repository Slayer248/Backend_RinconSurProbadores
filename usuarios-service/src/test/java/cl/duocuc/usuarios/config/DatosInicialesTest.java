package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_TablaVacia_CreaAdminYAsistentesConCambioObligatorio() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase(anyString())).thenReturn(false);

        datosIniciales.run();

        ArgumentCaptor<Usuario> guardados = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(guardados.capture());
        assertEquals("admin", guardados.getAllValues().get(0).getUsername());
        assertTrue(guardados.getAllValues().stream().allMatch(Usuario::getDebeCambiarContrasena));
    }

    @Test
    void run_DatosYaCargados_NoCreaNada() {
        when(usuarioRepository.count()).thenReturn(3L);
        when(usuarioRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);

        datosIniciales.run();

        verify(usuarioRepository, never()).save(any());
    }
}
