package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.SesionRequestDTO;
import cr.ac.ucr.edumentor.dto.SesionResponseDTO;
import cr.ac.ucr.edumentor.service.SesionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    private final SesionService sesionService;

    public SesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @PostMapping
    public ResponseEntity<SesionResponseDTO> reservar(@Valid @RequestBody SesionRequestDTO dto,
                                                      Authentication autenticacion) {
        SesionResponseDTO creada = sesionService.reservar(dto, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/mias")
    public List<SesionResponseDTO> listarMisSesiones(Authentication autenticacion) {
        return sesionService.listarMisSesiones(autenticacion.getName());
    }

    @PutMapping("/{id}/confirmar")
    public SesionResponseDTO confirmar(@PathVariable Long id, Authentication autenticacion) {
        return sesionService.confirmar(id, autenticacion.getName());
    }

    @PutMapping("/{id}/rechazar")
    public SesionResponseDTO rechazar(@PathVariable Long id, Authentication autenticacion) {
        return sesionService.rechazar(id, autenticacion.getName());
    }

    @PutMapping("/{id}/cancelar")
    public SesionResponseDTO cancelar(@PathVariable Long id, Authentication autenticacion) {
        return sesionService.cancelar(id, autenticacion.getName());
    }

    @PutMapping("/{id}/iniciar")
    public SesionResponseDTO iniciar(@PathVariable Long id, Authentication autenticacion) {
        return sesionService.iniciar(id, autenticacion.getName());
    }

    @PutMapping("/{id}/completar")
    public SesionResponseDTO completar(@PathVariable Long id, Authentication autenticacion) {
        return sesionService.completar(id, autenticacion.getName());
    }
}