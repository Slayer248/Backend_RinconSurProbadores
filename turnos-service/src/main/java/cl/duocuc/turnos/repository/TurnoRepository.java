package cl.duocuc.turnos.repository;

import cl.duocuc.turnos.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByEstadoOrderByEsPreferenteDescIdAsc(String estado);

    Optional<Turno> findFirstByEstadoOrderByEsPreferenteDescIdAsc(String estado);

    boolean existsByNombreClienteIgnoreCaseAndEstado(String nombreCliente, String estado);

    boolean existsByEstado(String estado);
}