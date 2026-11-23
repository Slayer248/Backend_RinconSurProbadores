package cl.duocuc.probadores.repository;

import cl.duocuc.probadores.model.UsoProbador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsoProbadorRepository extends JpaRepository<UsoProbador, Long> {

    List<UsoProbador> findByCodigoProbadorOrderByInicioAsc(String codigoProbador);

    Optional<UsoProbador> findFirstByCodigoProbadorAndFinIsNullOrderByInicioDesc(String codigoProbador);

    void deleteByCodigoProbador(String codigoProbador);
}
