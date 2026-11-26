package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.RecursoRequestDTO;
import cr.ac.ucr.edumentor.dto.RecursoResponseDTO;
import cr.ac.ucr.edumentor.service.RecursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping("/sesiones/{sesionId}/recursos")
    public List<RecursoResponseDTO> listarDeSesion(@PathVariable Long sesionId, Authentication autenticacion) {
        return recursoService.listarDeSesion(sesionId, autenticacion.getName());
    }

    @PostMapping("/sesiones/{sesionId}/recursos")
    public ResponseEntity<RecursoResponseDTO> crear(@PathVariable Long sesionId,
                                                    @Valid @RequestBody RecursoRequestDTO dto,
                                                    Authentication autenticacion) {
        RecursoResponseDTO creado = recursoService.crear(sesionId, dto, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/recursos/{recursoId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long recursoId, Authentication autenticacion) {
        recursoService.eliminar(recursoId, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
