package cl.duocuc.probadores.config;

import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatosInicialesTest {

    @Mock
    private ProbadorRepository probadorRepository;

    @InjectMocks
    private DatosIniciales datosIniciales;

    @Test
    void run_TablaVacia_InsertaBaseYProbadoresDemo() {
        when(probadorRepository.count()).thenReturn(0L);
        when(probadorRepository.existsById(anyString())).thenReturn(false);

        datosIniciales.run();

        verify(probadorRepository).saveAll(anyList());
        verify(probadorRepository, times(2)).save(any(Probador.class));
    }

    @Test
    void run_DatosYaCargados_NoInserta() {
        when(probadorRepository.count()).thenReturn(5L);
        when(probadorRepository.existsById(anyString())).thenReturn(true);

        datosIniciales.run();

        verify(probadorRepository, never()).saveAll(anyList());
        verify(probadorRepository, never()).save(any(Probador.class));
    }
}
