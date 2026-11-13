package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.DisponibilidadTutor;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DisponibilidadResponseDTO(
        Long id,
        Long tutorId,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Boolean estaActivo
) {
    public static DisponibilidadResponseDTO desde(DisponibilidadTutor bloque) {
        return new DisponibilidadResponseDTO(
                bloque.getId(),
                bloque.getTutor().getId(),
                bloque.getDiaSemana(),
                bloque.getHoraInicio(),
                bloque.getHoraFin(),
                bloque.getEstaActivo()
        );
    }
}
