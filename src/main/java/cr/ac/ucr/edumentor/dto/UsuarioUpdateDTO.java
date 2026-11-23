package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede pasar de 60 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60, message = "El apellido no puede pasar de 60 caracteres")
        String apellido,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
