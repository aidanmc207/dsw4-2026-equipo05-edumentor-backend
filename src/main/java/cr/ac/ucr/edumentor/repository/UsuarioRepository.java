package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByApellidoAscNombreAsc();

    List<Usuario> findByRolOrderByApellidoAscNombreAsc(Rol rol);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCarne(String carne);
}
