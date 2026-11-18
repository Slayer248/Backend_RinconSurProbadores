package cl.duocuc.probadores.repository;

import cl.duocuc.probadores.model.Probador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProbadorRepository extends JpaRepository<Probador, String> {

    List<Probador> findAllByOrderByCodigoAsc();

    List<Probador> findByEstadoIgnoreCaseOrderByCodigoAsc(String estado);
}