package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.EvaluacionTutoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluacionTutoriaRepository extends JpaRepository<EvaluacionTutoria, Long> {

    Optional<EvaluacionTutoria> findBySesionId(Long sesionId);

    boolean existsBySesionId(Long sesionId);

    List<EvaluacionTutoria> findBySesionTutorIdOrderByFechaEvaluacionDesc(Long tutorId);
}
