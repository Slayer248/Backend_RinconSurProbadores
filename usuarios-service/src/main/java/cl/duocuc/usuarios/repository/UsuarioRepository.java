package cl.duocuc.usuarios.repository;

import cl.duocuc.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    List<Usuario> findAllByOrderByIdAsc();
}