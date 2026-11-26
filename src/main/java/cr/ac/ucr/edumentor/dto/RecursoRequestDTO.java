package cr.ac.ucr.edumentor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RecursoRequestDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede pasar de 150 caracteres")
        String titulo,

        // Solo se aceptan enlaces web, así no se guardan cosas como "javascript:..."
        @NotBlank(message = "El enlace es obligatorio")
        @Size(max = 500, message = "El enlace no puede pasar de 500 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "El enlace debe empezar con http:// o https://")
        String urlRecurso
) {
}
