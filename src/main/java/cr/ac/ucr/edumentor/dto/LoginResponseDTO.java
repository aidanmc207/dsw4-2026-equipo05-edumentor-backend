package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Rol;

public record LoginResponseDTO(
        String token,
        Rol rol,
        String nombre
) {
}
