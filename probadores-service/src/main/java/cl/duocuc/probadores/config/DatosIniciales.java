package cl.duocuc.probadores.config;

import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ProbadorRepository probadorRepository;

    @Override
    public void run(String... args) {
        if (probadorRepository.count() == 0) {
            probadorRepository.saveAll(List.of(
                    Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").desdeHora("-").build(),
                    Probador.builder().codigo("P-02").esAccesible(false).estado("LIBRE").desdeHora("-").build(),
                    Probador.builder().codigo("P-03").esAccesible(false).estado("LIBRE").desdeHora("-").build()
            ));
        }
    }
}