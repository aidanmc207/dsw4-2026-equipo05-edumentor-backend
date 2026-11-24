package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Modalidad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SesionRequestDTO(
        @NotNull(message = "El tutor es obligatorio")
        Long tutorId,

        @NotNull(message = "La materia es obligatoria")
        Long materiaId,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaHora,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 30, message = "La sesión debe durar al menos 30 minutos")
        @Max(value = 240, message = "La sesión no puede durar más de 240 minutos")
        Integer duracionMinutos,

        @NotNull(message = "La modalidad es obligatoria")
        Modalidad modalidad
) {
}