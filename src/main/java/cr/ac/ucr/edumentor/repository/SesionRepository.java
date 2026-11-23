package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.SesionTutoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionRepository extends JpaRepository<SesionTutoria, Long> {

    boolean existsByMateriaId(Long materiaId);
}
