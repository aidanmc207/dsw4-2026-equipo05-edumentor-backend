package cr.ac.ucr.edumentor.repository;

import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<SesionTutoria, Long> {

    boolean existsByMateriaId(Long materiaId);

    boolean existsByTutorIdOrEstudianteId(Long tutorId, Long estudianteId);

    @Query("select s from SesionTutoria s "
            + "join fetch s.estudiante join fetch s.tutor join fetch s.materia "
            + "where s.id = :id")
    Optional<SesionTutoria> buscarConDetalle(@Param("id") Long id);

    @Query("select s from SesionTutoria s "
            + "join fetch s.estudiante join fetch s.tutor join fetch s.materia "
            + "where s.estudiante.id = :usuarioId or s.tutor.id = :usuarioId "
            + "order by s.fechaHora desc")
    List<SesionTutoria> listarPorParticipante(@Param("usuarioId") Long usuarioId);

    @Query("select s from SesionTutoria s "
            + "where s.estado = :estado "
            + "and (s.tutor.id = :usuarioId or s.estudiante.id = :usuarioId) "
            + "and s.fechaHora >= :desde and s.fechaHora < :hasta")
    List<SesionTutoria> buscarDelUsuarioEnRango(@Param("usuarioId") Long usuarioId,
                                                @Param("estado") EstadoSesion estado,
                                                @Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDateTime hasta);
}