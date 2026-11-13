package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String carne,
        String email,
        String nombre,
        String apellido,
        Rol rol,
        Double promedioCalificacion,
        Integer totalEvaluaciones
) {
    public static UsuarioResponseDTO desde(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getCarne(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getPromedioCalificacion(),
                usuario.getTotalEvaluaciones()
        );
    }
}
