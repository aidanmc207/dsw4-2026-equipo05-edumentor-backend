package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.SesionRequestDTO;
import cr.ac.ucr.edumentor.dto.SesionResponseDTO;
import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.MateriaRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SesionService {

    private static final long HORAS_MINIMAS_CANCELACION = 2;

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final DisponibilidadService disponibilidadService;

    public SesionService(SesionRepository sesionRepository, UsuarioRepository usuarioRepository,
                         MateriaRepository materiaRepository, DisponibilidadService disponibilidadService) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
        this.disponibilidadService = disponibilidadService;
    }

    @Transactional
    public SesionResponseDTO reservar(SesionRequestDTO dto, String emailUsuario) {
        Usuario estudiante = buscarPorEmail(emailUsuario);
        if (estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un estudiante puede reservar sesiones");
        }
        Usuario tutor = usuarioRepository.findById(dto.tutorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tutor con id " + dto.tutorId()));
        if (tutor.getRol() != Rol.TUTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario con id " + dto.tutorId() + " no es tutor");
        }
        Materia materia = materiaRepository.findById(dto.materiaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la materia con id " + dto.materiaId()));
        if (!dto.fechaHora().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de la sesión debe ser futura");
        }
        if (!disponibilidadService.cubreHorario(tutor.getId(), dto.fechaHora(), dto.duracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tutor no tiene disponibilidad en ese horario");
        }
        validarTraslape(tutor, "El tutor", dto.fechaHora(), dto.duracionMinutos());
        validarTraslape(estudiante, "El estudiante", dto.fechaHora(), dto.duracionMinutos());

        SesionTutoria sesion = new SesionTutoria(estudiante, tutor, materia,
                dto.fechaHora(), dto.duracionMinutos(), dto.modalidad());
        return SesionResponseDTO.desde(sesionRepository.save(sesion));
    }

    @Transactional
    public SesionResponseDTO confirmar(Long sesionId, String emailUsuario) {
        SesionTutoria sesion = buscarSesion(sesionId);
        validarTutor(sesion, emailUsuario);
        exigirEstado(sesion, EstadoSesion.REQUESTED, "confirmar");
        validarTraslape(sesion.getTutor(), "El tutor", sesion.getFechaHora(), sesion.getDuracionMinutos());
        validarTraslape(sesion.getEstudiante(), "El estudiante", sesion.getFechaHora(), sesion.getDuracionMinutos());
        sesion.setEstado(EstadoSesion.CONFIRMED);
        return SesionResponseDTO.desde(sesion);
    }

    @Transactional
    public SesionResponseDTO rechazar(Long sesionId, String emailUsuario) {
        SesionTutoria sesion = buscarSesion(sesionId);
        validarTutor(sesion, emailUsuario);
        exigirEstado(sesion, EstadoSesion.REQUESTED, "rechazar");
        sesion.setEstado(EstadoSesion.REJECTED);
        return SesionResponseDTO.desde(sesion);
    }

    @Transactional
    public SesionResponseDTO cancelar(Long sesionId, String emailUsuario) {
        SesionTutoria sesion = buscarSesion(sesionId);
        boolean esEstudiante = sesion.getEstudiante().getEmail().equals(emailUsuario);
        boolean esTutor = sesion.getTutor().getEmail().equals(emailUsuario);
        if (!esEstudiante && !esTutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el estudiante o el tutor de la sesión pueden cancelarla");
        }
        if (sesion.getEstado() == EstadoSesion.REQUESTED) {
            if (!esEstudiante) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el estudiante puede retirar su solicitud");
            }
        } else if (sesion.getEstado() == EstadoSesion.CONFIRMED) {
            if (LocalDateTime.now().plusHours(HORAS_MINIMAS_CANCELACION).isAfter(sesion.getFechaHora())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede cancelar con al menos 2 horas de anticipación");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar una sesión en estado " + sesion.getEstado());
        }
        sesion.setEstado(EstadoSesion.CANCELLED);
        return SesionResponseDTO.desde(sesion);
    }

    @Transactional
    public SesionResponseDTO iniciar(Long sesionId, String emailUsuario) {
        SesionTutoria sesion = buscarSesion(sesionId);
        validarTutor(sesion, emailUsuario);
        exigirEstado(sesion, EstadoSesion.CONFIRMED, "iniciar");
        if (LocalDateTime.now().isBefore(sesion.getFechaHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Todavía no es la hora de la sesión");
        }
        sesion.setEstado(EstadoSesion.IN_SESSION);
        return SesionResponseDTO.desde(sesion);
    }

    @Transactional
    public SesionResponseDTO completar(Long sesionId, String emailUsuario) {
        SesionTutoria sesion = buscarSesion(sesionId);
        validarTutor(sesion, emailUsuario);
        exigirEstado(sesion, EstadoSesion.IN_SESSION, "completar");
        sesion.setEstado(EstadoSesion.COMPLETED);
        return SesionResponseDTO.desde(sesion);
    }

    @Transactional(readOnly = true)
    public List<SesionResponseDTO> listarMisSesiones(String emailUsuario) {
        Usuario usuario = buscarPorEmail(emailUsuario);
        return sesionRepository.listarPorParticipante(usuario.getId()).stream()
                .map(SesionResponseDTO::desde)
                .toList();
    }

    private void validarTraslape(Usuario usuario, String quien, LocalDateTime inicio, Integer duracionMinutos) {
        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);
        LocalDateTime desde = inicio.toLocalDate().atStartOfDay();
        LocalDateTime hasta = desde.plusDays(1);
        boolean choca = sesionRepository
                .buscarDelUsuarioEnRango(usuario.getId(), EstadoSesion.CONFIRMED, desde, hasta).stream()
                .anyMatch(otra -> inicio.isBefore(otra.getFechaHora().plusMinutes(otra.getDuracionMinutos()))
                        && fin.isAfter(otra.getFechaHora()));
        if (choca) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, quien + " ya tiene una sesión confirmada que se traslapa con ese horario");
        }
    }

    private void validarTutor(SesionTutoria sesion, String emailUsuario) {
        if (!sesion.getTutor().getEmail().equals(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el tutor de la sesión puede hacer esto");
        }
    }

    private void exigirEstado(SesionTutoria sesion, EstadoSesion esperado, String accion) {
        if (sesion.getEstado() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede " + accion + " una sesión en estado " + sesion.getEstado());
        }
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario " + email));
    }

    private SesionTutoria buscarSesion(Long id) {
        return sesionRepository.buscarConDetalle(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la sesión con id " + id));
    }
}