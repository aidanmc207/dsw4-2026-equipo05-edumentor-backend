package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.Modalidad;
import cr.ac.ucr.edumentor.entity.SesionTutoria;

import java.time.LocalDateTime;

public record SesionResponseDTO(
        Long id,
        String nombreEstudiante,
        String nombreTutor,
        String nombreMateria,
        LocalDateTime fechaHora,
        Integer duracionMinutos,
        EstadoSesion estado,
        Modalidad modalidad
) {
    public static SesionResponseDTO desde(SesionTutoria sesion) {
        return new SesionResponseDTO(
                sesion.getId(),
                sesion.getEstudiante().getNombre() + " " + sesion.getEstudiante().getApellido(),
                sesion.getTutor().getNombre() + " " + sesion.getTutor().getApellido(),
                sesion.getMateria().getNombreMateria(),
                sesion.getFechaHora(),
                sesion.getDuracionMinutos(),
                sesion.getEstado(),
                sesion.getModalidad()
        );
    }
}