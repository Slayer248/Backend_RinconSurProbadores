package cl.duocuc.probadoresinhabilitados.repository;

import cl.duocuc.probadoresinhabilitados.model.ProbadorInhabilitado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProbadorInhabilitadoRepository extends JpaRepository<ProbadorInhabilitado, Long> {

    List<ProbadorInhabilitado> findAllByOrderByFechaInhabilitacionDesc();
}
