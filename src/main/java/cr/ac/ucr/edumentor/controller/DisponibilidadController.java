package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.DisponibilidadRequestDTO;
import cr.ac.ucr.edumentor.dto.DisponibilidadResponseDTO;
import cr.ac.ucr.edumentor.service.DisponibilidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/tutores/{tutorId}/disponibilidad")
    public List<DisponibilidadResponseDTO> listarPorTutor(@PathVariable Long tutorId) {
        return disponibilidadService.listarPorTutor(tutorId);
    }

    @PostMapping("/tutores/{tutorId}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> crear(@PathVariable Long tutorId,
                                                           @Valid @RequestBody DisponibilidadRequestDTO dto,
                                                           Authentication autenticacion) {
        DisponibilidadResponseDTO creado = disponibilidadService.crear(tutorId, dto, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/disponibilidad/{id}")
    public DisponibilidadResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody DisponibilidadRequestDTO dto,
                                                Authentication autenticacion) {
        return disponibilidadService.actualizar(id, dto, autenticacion.getName());
    }

    @DeleteMapping("/disponibilidad/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        disponibilidadService.eliminar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
