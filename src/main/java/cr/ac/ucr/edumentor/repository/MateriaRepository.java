package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    List<Materia> findAllByOrderByNombreMateriaAsc();

    boolean existsByCodigoMateria(String codigoMateria);

    boolean existsByCodigoMateriaAndIdNot(String codigoMateria, Long id);
}
