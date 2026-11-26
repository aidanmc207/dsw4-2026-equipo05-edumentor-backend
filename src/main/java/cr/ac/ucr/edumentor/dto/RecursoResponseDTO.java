package cr.ac.ucr.edumentor.dto;

import cr.ac.ucr.edumentor.entity.RecursoEstudio;

public record RecursoResponseDTO(
        Long id,
        Long sesionId,
        String titulo,
        String urlRecurso
) {
    public static RecursoResponseDTO desde(RecursoEstudio recurso) {
        return new RecursoResponseDTO(
                recurso.getId(),
                recurso.getSesion().getId(),
                recurso.getTitulo(),
                recurso.getUrlRecurso()
        );
    }
}
