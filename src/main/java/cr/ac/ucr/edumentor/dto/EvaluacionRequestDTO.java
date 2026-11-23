package cr.ac.ucr.edumentor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluacionRequestDTO(
        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación debe ser al menos 1")
        @Max(value = 5, message = "La calificación no puede pasar de 5")
        Integer calificacionEstrellas,

        @Size(max = 500, message = "El comentario no puede pasar de 500 caracteres")
        String comentario
) {
}
