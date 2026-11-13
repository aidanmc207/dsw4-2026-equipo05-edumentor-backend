package cr.ac.ucr.edumentor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MateriaRequestDTO(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 10, message = "El código no puede pasar de 10 caracteres")
        String codigoMateria,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombreMateria,

        @NotBlank(message = "La carrera es obligatoria")
        @Size(max = 100, message = "La carrera no puede pasar de 100 caracteres")
        String carrera,

        @NotNull(message = "Los créditos son obligatorios")
        @Min(value = 1, message = "Los créditos deben ser al menos 1")
        @Max(value = 12, message = "Los créditos no pueden pasar de 12")
        Integer creditos
) {
}
