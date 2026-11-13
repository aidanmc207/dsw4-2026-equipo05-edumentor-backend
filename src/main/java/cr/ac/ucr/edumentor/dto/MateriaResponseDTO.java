package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.Materia;

public record MateriaResponseDTO(
        Long id,
        String codigoMateria,
        String nombreMateria,
        String carrera,
        Integer creditos
) {
    public static MateriaResponseDTO desde(Materia materia) {
        return new MateriaResponseDTO(
                materia.getId(),
                materia.getCodigoMateria(),
                materia.getNombreMateria(),
                materia.getCarrera(),
                materia.getCreditos()
        );
    }
}
