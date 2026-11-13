package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.DisponibilidadRequestDTO;
import cr.ac.ucr.edumentor.dto.DisponibilidadResponseDTO;
import cr.ac.ucr.edumentor.entity.DisponibilidadTutor;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.DisponibilidadRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final UsuarioRepository usuarioRepository;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository, UsuarioRepository usuarioRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadResponseDTO> listarPorTutor(Long tutorId) {
        buscarTutor(tutorId);
        // Se ordena en Java porque en la base el día se guarda como texto y quedaría en orden alfabético
        return disponibilidadRepository.findByTutorId(tutorId).stream()
                .sorted(Comparator.comparing(DisponibilidadTutor::getDiaSemana)
                        .thenComparing(DisponibilidadTutor::getHoraInicio))
                .map(DisponibilidadResponseDTO::desde)
                .toList();
    }

    @Transactional
    public DisponibilidadResponseDTO crear(Long tutorId, DisponibilidadRequestDTO dto) {
        Usuario tutor = buscarTutor(tutorId);
        DisponibilidadTutor bloque = new DisponibilidadTutor(tutor, dto.diaSemana(), dto.horaInicio(), dto.horaFin());
        bloque.setEstaActivo(dto.estaActivo() == null || dto.estaActivo());
        validarBloque(tutorId, bloque, null);
        return DisponibilidadResponseDTO.desde(disponibilidadRepository.save(bloque));
    }

    @Transactional
    public DisponibilidadResponseDTO actualizar(Long id, DisponibilidadRequestDTO dto) {
        DisponibilidadTutor bloque = buscarBloque(id);
        bloque.setDiaSemana(dto.diaSemana());
        bloque.setHoraInicio(dto.horaInicio());
        bloque.setHoraFin(dto.horaFin());
        if (dto.estaActivo() != null) {
            bloque.setEstaActivo(dto.estaActivo());
        }
        validarBloque(bloque.getTutor().getId(), bloque, id);
        return DisponibilidadResponseDTO.desde(bloque);
    }

    @Transactional
    public void eliminar(Long id) {
        disponibilidadRepository.delete(buscarBloque(id));
    }

    // Lo usa la reserva de sesiones: la sesión tiene que caber completa dentro de un bloque activo del tutor
    @Transactional(readOnly = true)
    public boolean cubreHorario(Long tutorId, LocalDateTime fechaHora, int duracionMinutos) {
        LocalDateTime fin = fechaHora.plusMinutes(duracionMinutos);
        if (!fin.toLocalDate().equals(fechaHora.toLocalDate())) {
            return false;
        }
        LocalTime horaInicio = fechaHora.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        return disponibilidadRepository.findByTutorIdAndDiaSemana(tutorId, fechaHora.getDayOfWeek()).stream()
                .filter(DisponibilidadTutor::getEstaActivo)
                .anyMatch(b -> !horaInicio.isBefore(b.getHoraInicio()) && !horaFin.isAfter(b.getHoraFin()));
    }

    private void validarBloque(Long tutorId, DisponibilidadTutor bloque, Long idExcluido) {
        LocalTime inicio = bloque.getHoraInicio();
        LocalTime fin = bloque.getHoraFin();
        if (!fin.isAfter(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la hora de inicio");
        }
        // Un bloque inactivo no ocupa horario, así que no se revisa el traslape
        if (!bloque.getEstaActivo()) {
            return;
        }
        boolean choca = disponibilidadRepository.findByTutorIdAndDiaSemana(tutorId, bloque.getDiaSemana()).stream()
                .filter(otro -> otro.getEstaActivo() && !otro.getId().equals(idExcluido))
                .anyMatch(otro -> inicio.isBefore(otro.getHoraFin()) && fin.isAfter(otro.getHoraInicio()));
        if (choca) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El bloque choca con otro horario del tutor ese mismo día");
        }
    }

    private Usuario buscarTutor(Long tutorId) {
        Usuario usuario = usuarioRepository.findById(tutorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario con id " + tutorId));
        if (usuario.getRol() != Rol.TUTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario con id " + tutorId + " no es tutor");
        }
        return usuario;
    }

    private DisponibilidadTutor buscarBloque(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el bloque de disponibilidad con id " + id));
    }
}
