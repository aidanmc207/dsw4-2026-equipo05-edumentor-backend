package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.EvaluacionRequestDTO;
import cr.ac.ucr.edumentor.dto.EvaluacionResponseDTO;
import cr.ac.ucr.edumentor.service.EvaluacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @PostMapping("/sesiones/{sesionId}/evaluacion")
    public ResponseEntity<EvaluacionResponseDTO> evaluar(@PathVariable Long sesionId,
                                                         @Valid @RequestBody EvaluacionRequestDTO dto,
                                                         Authentication autenticacion) {
        EvaluacionResponseDTO creada = evaluacionService.crear(sesionId, dto, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/sesiones/{sesionId}/evaluacion")
    public EvaluacionResponseDTO obtenerDeSesion(@PathVariable Long sesionId, Authentication autenticacion) {
        return evaluacionService.obtenerPorSesion(sesionId, autenticacion.getName());
    }

    @GetMapping("/tutores/{tutorId}/evaluaciones")
    public List<EvaluacionResponseDTO> listarDeTutor(@PathVariable Long tutorId) {
        return evaluacionService.listarDeTutor(tutorId);
    }
}
