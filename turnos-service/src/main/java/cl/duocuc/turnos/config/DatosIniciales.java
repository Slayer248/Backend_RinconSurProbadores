package cl.duocuc.turnos.config;

import cl.duocuc.turnos.model.Turno;
import cl.duocuc.turnos.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final String EN_ESPERA = "EN_ESPERA";

    private final TurnoRepository turnoRepository;

    @Value("${app.datos-iniciales.demo:true}")
    private boolean cargarDemo = true;

    @Override
    public void run(String... args) {
        if (!cargarDemo || turnoRepository.existsByEstado(EN_ESPERA)) {
            return;
        }

        long correlativo = 1001L + turnoRepository.count();
        turnoRepository.saveAll(List.of(
                turno(correlativo, "Ana Morales", 3, false),
                turno(correlativo + 1, "Luis Herrera", 2, false),
                turno(correlativo + 2, "Carmen Soto", 5, true),
                turno(correlativo + 3, "Diego Fuentes", 1, false)
        ));
    }

    private Turno turno(long numero, String cliente, int prendas, boolean preferente) {
        return Turno.builder()
                .numeroTurno("T-" + numero)
                .nombreCliente(cliente)
                .cantidadPrendas(prendas)
                .esPreferente(preferente)
                .estado(EN_ESPERA)
                .build();
    }
}
