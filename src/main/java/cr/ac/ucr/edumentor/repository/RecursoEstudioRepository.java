package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.RecursoEstudio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecursoEstudioRepository extends JpaRepository<RecursoEstudio, Long> {

    List<RecursoEstudio> findBySesionIdOrderByIdAsc(Long sesionId);
}
