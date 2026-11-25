package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.MateriaRequestDTO;
import cr.ac.ucr.edumentor.dto.MateriaResponseDTO;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.repository.MateriaRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final SesionRepository sesionRepository;

    public MateriaService(MateriaRepository materiaRepository, SesionRepository sesionRepository) {
        this.materiaRepository = materiaRepository;
        this.sesionRepository = sesionRepository;
    }

    @Transactional(readOnly = true)
    public List<MateriaResponseDTO> listar() {
        return materiaRepository.findAllByOrderByNombreMateriaAsc().stream()
                .map(MateriaResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public MateriaResponseDTO obtener(Long id) {
        return MateriaResponseDTO.desde(buscarMateria(id));
    }

    @Transactional
    public MateriaResponseDTO crear(MateriaRequestDTO dto) {
        String codigo = dto.codigoMateria().trim().toUpperCase();
        if (materiaRepository.existsByCodigoMateria(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una materia con el código " + codigo);
        }
        Materia materia = new Materia(codigo, dto.nombreMateria().trim(), dto.carrera().trim(), dto.creditos());
        return MateriaResponseDTO.desde(materiaRepository.save(materia));
    }

    @Transactional
    public MateriaResponseDTO actualizar(Long id, MateriaRequestDTO dto) {
        Materia materia = buscarMateria(id);
        String codigo = dto.codigoMateria().trim().toUpperCase();
        if (materiaRepository.existsByCodigoMateriaAndIdNot(codigo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una materia con el código " + codigo);
        }
        materia.setCodigoMateria(codigo);
        materia.setNombreMateria(dto.nombreMateria().trim());
        materia.setCarrera(dto.carrera().trim());
        materia.setCreditos(dto.creditos());
        return MateriaResponseDTO.desde(materia);
    }

    @Transactional
    public void eliminar(Long id) {
        Materia materia = buscarMateria(id);
        if (sesionRepository.existsByMateriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar " + materia.getCodigoMateria() + " porque tiene sesiones registradas");
        }
        materiaRepository.delete(materia);
    }

    private Materia buscarMateria(Long id) {
        return materiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la materia con id " + id));
    }
}
