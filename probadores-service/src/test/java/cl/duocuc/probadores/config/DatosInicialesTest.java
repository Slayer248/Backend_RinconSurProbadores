package cl.duocuc.probadores.config;

import cl.duocuc.probadores.repository.ProbadorRepository;
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
    private ProbadorRepository probadorRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_TablaVacia_InsertaTresProbadores() {
        when(probadorRepository.count()).thenReturn(0L);

        datosIniciales.run();

        verify(probadorRepository).saveAll(anyList());
    }

    @Test
    void run_TablaConDatos_NoInserta() {
        when(probadorRepository.count()).thenReturn(3L);

        datosIniciales.run();

        verify(probadorRepository, never()).saveAll(anyList());
    }
}