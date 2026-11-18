package cl.duocuc.probadores.config;

import cl.duocuc.probadores.model.Probador;
import cl.duocuc.probadores.repository.ProbadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ProbadorRepository probadorRepository;

    @Value("${app.datos-iniciales.demo:true}")
    private boolean cargarDemo = true;

    @Override
    public void run(String... args) {
        if (probadorRepository.count() == 0) {
            probadorRepository.saveAll(List.of(
                    Probador.builder().codigo("P-01").esAccesible(false).estado("LIBRE").desdeHora("-").build(),
                    Probador.builder().codigo("P-02").esAccesible(false).estado("LIBRE").desdeHora("-").build(),
                    Probador.builder().codigo("P-03").esAccesible(false).estado("LIBRE").desdeHora("-").build()
            ));
        }

        if (cargarDemo) {
            crearSiNoExiste(Probador.builder().codigo("P-04").esAccesible(true).estado("LIBRE").desdeHora("-")
                    .ultimoCliente("Camila Rojas").ultimoTurno("T-0998").ultimoUso("17-11-2026 18:40").build());
            crearSiNoExiste(Probador.builder().codigo("P-05").esAccesible(false).estado("LIBRE").desdeHora("-")
                    .ultimoCliente("Jorge Vidal").ultimoTurno("T-0999").ultimoUso("17-11-2026 19:05").build());
        }
    }

    private void crearSiNoExiste(Probador probador) {
        if (!probadorRepository.existsById(probador.getCodigo())) {
            probadorRepository.save(probador);
        }
    }
}
