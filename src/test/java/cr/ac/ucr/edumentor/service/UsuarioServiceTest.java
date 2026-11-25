package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.UsuarioResponseDTO;
import cr.ac.ucr.edumentor.dto.UsuarioUpdateDTO;
import cr.ac.ucr.edumentor.entity.DisponibilidadTutor;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.DisponibilidadRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private SesionRepository sesionRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void actualizarCambiaNombreYRol() {
        Usuario estudiante = new Usuario("C20001", "estudiante@ucr.ac.cr", "hash", "Sofía", "Araya", Rol.ESTUDIANTE);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(estudiante));

        UsuarioResponseDTO actualizado = usuarioService.actualizar(4L, new UsuarioUpdateDTO(" Sofía ", "Araya Mora", Rol.TUTOR));

        assertEquals("Sofía", actualizado.nombre());
        assertEquals("Araya Mora", actualizado.apellido());
        assertEquals(Rol.TUTOR, actualizado.rol());
    }

    @Test
    void coordinadorNoPuedeEliminarSuPropiaCuenta() {
        Usuario coordinador = new Usuario("C00001", "coordinador@ucr.ac.cr", "hash", "Laura", "Quesada", Rol.COORDINADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(coordinador));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.eliminar(1L, "coordinador@ucr.ac.cr"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void eliminarTutorConSesionesDevuelve409() {
        Usuario tutor = new Usuario("B90001", "tutor@ucr.ac.cr", "hash", "Mario", "Solano", Rol.TUTOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(tutor));
        when(sesionRepository.existsByTutorIdOrEstudianteId(2L, 2L)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> usuarioService.eliminar(2L, "coordinador@ucr.ac.cr"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void eliminarTutorSinSesionesBorraTambienSusHorarios() {
        Usuario tutor = new Usuario("B90001", "tutor@ucr.ac.cr", "hash", "Mario", "Solano", Rol.TUTOR);
        List<DisponibilidadTutor> horarios = List.of(
                new DisponibilidadTutor(tutor, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(tutor));
        when(sesionRepository.existsByTutorIdOrEstudianteId(2L, 2L)).thenReturn(false);
        when(disponibilidadRepository.findByTutorId(2L)).thenReturn(horarios);

        usuarioService.eliminar(2L, "coordinador@ucr.ac.cr");

        verify(disponibilidadRepository).deleteAll(horarios);
        verify(usuarioRepository).delete(tutor);
    }

    @Test
    void obtenerUsuarioInexistenteDevuelve404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> usuarioService.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
