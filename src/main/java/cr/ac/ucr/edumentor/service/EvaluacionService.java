package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.EvaluacionRequestDTO;
import cr.ac.ucr.edumentor.dto.EvaluacionResponseDTO;
import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.EvaluacionTutoria;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.EvaluacionTutoriaRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EvaluacionService {

    private final EvaluacionTutoriaRepository evaluacionRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    public EvaluacionService(EvaluacionTutoriaRepository evaluacionRepository,
                             SesionRepository sesionRepository,
                             UsuarioRepository usuarioRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EvaluacionResponseDTO crear(Long sesionId, EvaluacionRequestDTO dto, String emailEstudiante) {
        SesionTutoria sesion = buscarSesion(sesionId);

        if (!sesion.getEstudiante().getEmail().equals(emailEstudiante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el estudiante de la sesión puede evaluarla");
        }
        if (evaluacionRepository.existsBySesionId(sesionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión ya fue evaluada");
        }
        if (sesion.getEstado() != EstadoSesion.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede evaluar una sesión completada");
        }

        String comentario = dto.comentario() == null || dto.comentario().isBlank() ? null : dto.comentario().trim();
        EvaluacionTutoria evaluacion = evaluacionRepository.save(
                new EvaluacionTutoria(sesion, dto.calificacionEstrellas(), comentario));

        sesion.setEstado(EstadoSesion.REVIEWED);
        recalcularPromedio(sesion.getTutor());
        return EvaluacionResponseDTO.desde(evaluacion);
    }

    @Transactional(readOnly = true)
    public EvaluacionResponseDTO obtenerPorSesion(Long sesionId, String email) {
        SesionTutoria sesion = buscarSesion(sesionId);

        boolean participa = sesion.getEstudiante().getEmail().equals(email)
                || sesion.getTutor().getEmail().equals(email);
        if (!participa) {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró su usuario"));
            if (usuario.getRol() != Rol.COORDINADOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para ver esta evaluación");
            }
        }

        return evaluacionRepository.findBySesionId(sesionId)
                .map(EvaluacionResponseDTO::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión todavía no tiene evaluación"));
    }

    @Transactional(readOnly = true)
    public List<EvaluacionResponseDTO> listarDeTutor(Long tutorId) {
        Usuario tutor = usuarioRepository.findById(tutorId)
                .filter(usuario -> usuario.getRol() == Rol.TUTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tutor con id " + tutorId));

        return evaluacionRepository.findBySesionTutorIdOrderByFechaEvaluacionDesc(tutor.getId()).stream()
                .map(EvaluacionResponseDTO::desde)
                .toList();
    }

    private void recalcularPromedio(Usuario tutor) {
        List<EvaluacionTutoria> evaluaciones =
                evaluacionRepository.findBySesionTutorIdOrderByFechaEvaluacionDesc(tutor.getId());
        double promedio = evaluaciones.stream()
                .mapToInt(EvaluacionTutoria::getCalificacionEstrellas)
                .average()
                .orElse(0.0);
        tutor.setPromedioCalificacion(Math.round(promedio * 100.0) / 100.0);
        tutor.setTotalEvaluaciones(evaluaciones.size());
    }

    private SesionTutoria buscarSesion(Long id) {
        return sesionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la sesión con id " + id));
    }
}
