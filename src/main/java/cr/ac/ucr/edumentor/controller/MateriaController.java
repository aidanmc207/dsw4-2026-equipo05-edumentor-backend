package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.MateriaRequestDTO;
import cr.ac.ucr.edumentor.dto.MateriaResponseDTO;
import cr.ac.ucr.edumentor.service.MateriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public List<MateriaResponseDTO> listar() {
        return materiaService.listar();
    }

    @GetMapping("/{id}")
    public MateriaResponseDTO obtener(@PathVariable Long id) {
        return materiaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<MateriaResponseDTO> crear(@Valid @RequestBody MateriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaService.crear(dto));
    }

    @PutMapping("/{id}")
    public MateriaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody MateriaRequestDTO dto) {
        return materiaService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        materiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
