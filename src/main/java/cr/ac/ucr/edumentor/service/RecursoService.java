package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.RecursoRequestDTO;
import cr.ac.ucr.edumentor.dto.RecursoResponseDTO;
import cr.ac.ucr.edumentor.entity.RecursoEstudio;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.RecursoEstudioRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecursoService {

    private final RecursoEstudioRepository recursoRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    public RecursoService(RecursoEstudioRepository recursoRepository,
                          SesionRepository sesionRepository,
                          UsuarioRepository usuarioRepository) {
        this.recursoRepository = recursoRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Los ven el estudiante y el tutor de la sesión, y el coordinador
    @Transactional(readOnly = true)
    public List<RecursoResponseDTO> listarDeSesion(Long sesionId, String email) {
        SesionTutoria sesion = buscarSesion(sesionId);

        boolean participa = sesion.getEstudiante().getEmail().equals(email)
                || sesion.getTutor().getEmail().equals(email);
        if (!participa) {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró su usuario"));
            if (usuario.getRol() != Rol.COORDINADOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para ver los recursos de esta sesión");
            }
        }

        return recursoRepository.findBySesionIdOrderByIdAsc(sesionId).stream()
                .map(RecursoResponseDTO::desde)
                .toList();
    }

    @Transactional
    public RecursoResponseDTO crear(Long sesionId, RecursoRequestDTO dto, String emailTutor) {
        SesionTutoria sesion = buscarSesion(sesionId);
        validarTutor(sesion, emailTutor, "Solo el tutor de la sesión puede agregar recursos");

        RecursoEstudio recurso = recursoRepository.save(
                new RecursoEstudio(sesion, dto.titulo().trim(), dto.urlRecurso().trim()));
        return RecursoResponseDTO.desde(recurso);
    }

    @Transactional
    public void eliminar(Long recursoId, String emailTutor) {
        RecursoEstudio recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el recurso con id " + recursoId));
        validarTutor(recurso.getSesion(), emailTutor, "Solo el tutor de la sesión puede eliminar sus recursos");

        recursoRepository.delete(recurso);
    }

    private void validarTutor(SesionTutoria sesion, String email, String mensaje) {
        if (!sesion.getTutor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensaje);
        }
    }

    private SesionTutoria buscarSesion(Long id) {
        return sesionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la sesión con id " + id));
    }
}
