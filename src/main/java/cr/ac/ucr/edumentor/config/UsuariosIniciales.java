package cr.ac.ucr.edumentor.config;

import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuariosIniciales implements CommandLineRunner {

    private static final String CLAVE_PRUEBA = "Edumentor2026";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(CLAVE_PRUEBA);
        usuarioRepository.saveAll(List.of(
                new Usuario("C00001", "coordinador@ucr.ac.cr", hash, "Laura", "Quesada", Rol.COORDINADOR),
                new Usuario("B90001", "tutor@ucr.ac.cr", hash, "Mario", "Solano", Rol.TUTOR),
                new Usuario("B90002", "tutora@ucr.ac.cr", hash, "Daniela", "Rojas", Rol.TUTOR),
                new Usuario("C20001", "estudiante@ucr.ac.cr", hash, "Sofía", "Araya", Rol.ESTUDIANTE)
        ));
    }
}
