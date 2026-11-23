package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.UsuarioResponseDTO;
import cr.ac.ucr.edumentor.dto.UsuarioUpdateDTO;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar(Rol rol) {
        List<Usuario> usuarios = rol == null
                ? usuarioRepository.findAllByOrderByApellidoAscNombreAsc()
                : usuarioRepository.findByRolOrderByApellidoAscNombreAsc(rol);
        return usuarios.stream().map(UsuarioResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTutores() {
        return listar(Rol.TUTOR);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtener(Long id) {
        return UsuarioResponseDTO.desde(buscarUsuario(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponseDTO::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró su usuario"));
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuario(id);
        usuario.setNombre(dto.nombre().trim());
        usuario.setApellido(dto.apellido().trim());
        usuario.setRol(dto.rol());
        return UsuarioResponseDTO.desde(usuario);
    }

    @Transactional
    public void eliminar(Long id, String emailCoordinador) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getEmail().equals(emailCoordinador)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puede eliminar su propia cuenta");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario con id " + id));
    }
}
