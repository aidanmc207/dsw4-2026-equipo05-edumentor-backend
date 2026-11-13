package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(
        @NotBlank(message = "El carné es obligatorio")
        @Size(max = 20, message = "El carné no puede pasar de 20 caracteres")
        String carne,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede pasar de 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

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
