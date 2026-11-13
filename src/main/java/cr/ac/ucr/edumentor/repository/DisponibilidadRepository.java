package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.DisponibilidadTutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<DisponibilidadTutor, Long> {

    List<DisponibilidadTutor> findByTutorId(Long tutorId);

    List<DisponibilidadTutor> findByTutorIdAndDiaSemana(Long tutorId, DayOfWeek diaSemana);
}
