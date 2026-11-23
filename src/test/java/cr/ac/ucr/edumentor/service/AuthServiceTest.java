package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.LoginRequestDTO;
import cr.ac.ucr.edumentor.dto.LoginResponseDTO;
import cr.ac.ucr.edumentor.dto.RegistroRequestDTO;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import cr.ac.ucr.edumentor.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario tutor() {
        return new Usuario("B90001", "tutor@ucr.ac.cr", "hash", "Mario", "Solano", Rol.TUTOR);
    }

    @Test
    void loginCorrectoDevuelveTokenYRol() {
        Usuario tutor = tutor();
        when(usuarioRepository.findByEmail("tutor@ucr.ac.cr")).thenReturn(Optional.of(tutor));
        when(passwordEncoder.matches("Edumentor2026", "hash")).thenReturn(true);
        when(jwtService.generarToken(tutor)).thenReturn("token-falso");

        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO("Tutor@ucr.ac.cr ", "Edumentor2026"));

        assertEquals("token-falso", respuesta.token());
        assertEquals(Rol.TUTOR, respuesta.rol());
        assertEquals("Mario", respuesta.nombre());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() {
        when(usuarioRepository.findByEmail("tutor@ucr.ac.cr")).thenReturn(Optional.of(tutor()));
        when(passwordEncoder.matches("otra", "hash")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequestDTO("tutor@ucr.ac.cr", "otra")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void registroNoPermiteCrearCoordinador() {
        RegistroRequestDTO dto = new RegistroRequestDTO("C21111", "nuevo@ucr.ac.cr", "12345678",
                "Pedro", "Mora", Rol.COORDINADOR);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.registrar(dto));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registroRechazaCorreoRepetido() {
        RegistroRequestDTO dto = new RegistroRequestDTO("C21111", "tutor@ucr.ac.cr", "12345678",
                "Pedro", "Mora", Rol.ESTUDIANTE);
        when(usuarioRepository.existsByEmail("tutor@ucr.ac.cr")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.registrar(dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }
}
