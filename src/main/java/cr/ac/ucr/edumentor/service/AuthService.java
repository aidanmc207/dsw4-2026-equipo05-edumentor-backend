package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.LoginRequestDTO;
import cr.ac.ucr.edumentor.dto.LoginResponseDTO;
import cr.ac.ucr.edumentor.dto.RegistroRequestDTO;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import cr.ac.ucr.edumentor.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        String email = dto.email().trim().toLowerCase();
        // Mismo mensaje en los dos casos para no revelar qué correos existen
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return crearRespuesta(usuario);
    }

    @Transactional
    public LoginResponseDTO registrar(RegistroRequestDTO dto) {
        if (dto.rol() == Rol.COORDINADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puede registrarse como estudiante o tutor");
        }

        String email = dto.email().trim();
        String carne = dto.carne().trim().toUpperCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una cuenta con el correo " + email);
        }
        if (usuarioRepository.existsByCarne(carne)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una cuenta con el carné " + carne);
        }

        Usuario usuario = new Usuario(carne, email, passwordEncoder.encode(dto.password()),
                dto.nombre().trim(), dto.apellido().trim(), dto.rol());
        return crearRespuesta(usuarioRepository.save(usuario));
    }

    private LoginResponseDTO crearRespuesta(Usuario usuario) {
        return new LoginResponseDTO(jwtService.generarToken(usuario), usuario.getRol(), usuario.getNombre());
    }
}
