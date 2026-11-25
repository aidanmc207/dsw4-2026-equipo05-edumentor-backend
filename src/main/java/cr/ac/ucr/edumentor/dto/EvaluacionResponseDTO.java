package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.EvaluacionTutoria;

import java.time.LocalDateTime;

public record EvaluacionResponseDTO(
        Long id,
        Long sesionId,
        Integer calificacionEstrellas,
        String comentario,
        LocalDateTime fechaEvaluacion
) {
    public static EvaluacionResponseDTO desde(EvaluacionTutoria evaluacion) {
        return new EvaluacionResponseDTO(
                evaluacion.getId(),
                evaluacion.getSesion().getId(),
                evaluacion.getCalificacionEstrellas(),
                evaluacion.getComentario(),
                evaluacion.getFechaEvaluacion()
        );
    }
}
