package cr.ac.ucr.edumentor.controller;

import cr.ac.ucr.edumentor.dto.UsuarioResponseDTO;
import cr.ac.ucr.edumentor.dto.UsuarioUpdateDTO;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar(@RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/tutores")
    public List<UsuarioResponseDTO> listarTutores() {
        return usuarioService.listarTutores();
    }

    // El filtro jwt deja el correo del usuario como nombre de la autenticación
    @GetMapping("/yo")
    public UsuarioResponseDTO miPerfil(Authentication autenticacion) {
        return usuarioService.obtenerPorEmail(autenticacion.getName());
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return usuarioService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        usuarioService.eliminar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
