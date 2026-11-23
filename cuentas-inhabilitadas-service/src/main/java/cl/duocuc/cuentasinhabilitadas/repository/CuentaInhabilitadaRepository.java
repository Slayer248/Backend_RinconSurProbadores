package cl.duocuc.cuentasinhabilitadas.repository;

import cl.duocuc.cuentasinhabilitadas.model.CuentaInhabilitada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaInhabilitadaRepository extends JpaRepository<CuentaInhabilitada, Long> {

    Optional<CuentaInhabilitada> findByUsuarioId(Long usuarioId);

    List<CuentaInhabilitada> findAllByOrderByFechaInhabilitacionDesc();
}
