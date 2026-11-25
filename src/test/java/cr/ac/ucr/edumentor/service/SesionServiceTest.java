package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.SesionRequestDTO;
import cr.ac.ucr.edumentor.dto.SesionResponseDTO;
import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.entity.Modalidad;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.MateriaRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionRepository sesionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private DisponibilidadService disponibilidadService;

    @InjectMocks
    private SesionService sesionService;

    private Usuario estudiante;
    private Usuario tutor;
    private Materia materia;
    private LocalDateTime manana9am;

    @BeforeEach
    void preparar() {
        estudiante = crearUsuario(4L, "estudiante@ucr.ac.cr", Rol.ESTUDIANTE);
        tutor = crearUsuario(2L, "tutor@ucr.ac.cr", Rol.TUTOR);
        materia = new Materia("IF1300", "Introducción a la Computación", "Informática Empresarial", 4);
        manana9am = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void reservaValidaCreaSesionEnEstadoRequested() {
        SesionRequestDTO dto = new SesionRequestDTO(2L, 1L, manana9am, 60, Modalidad.VIRTUAL);
        when(usuarioRepository.findByEmail("estudiante@ucr.ac.cr")).thenReturn(java.util.Optional.of(estudiante));
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(tutor));
        when(materiaRepository.findById(1L)).thenReturn(java.util.Optional.of(materia));
        when(disponibilidadService.cubreHorario(2L, manana9am, 60)).thenReturn(true);
        when(sesionRepository.buscarDelUsuarioEnRango(anyLong(), eq(EstadoSesion.CONFIRMED), any(), any()))
                .thenReturn(List.of());
        when(sesionRepository.save(any(SesionTutoria.class))).thenAnswer(inv -> inv.getArgument(0));

        SesionResponseDTO respuesta = sesionService.reservar(dto, "estudiante@ucr.ac.cr");

        assertEquals(EstadoSesion.REQUESTED, respuesta.estado());
        assertEquals("Mario Solano", respuesta.nombreTutor());
        verify(sesionRepository).save(any(SesionTutoria.class));
    }

    @Test
    void reservaQueSeTraslapaConSesionConfirmadaDelTutorSeRechaza() {
        SesionTutoria existente = new SesionTutoria(crearUsuario(5L, "otro@ucr.ac.cr", Rol.ESTUDIANTE),
                tutor, materia, manana9am, 60, Modalidad.VIRTUAL);
        existente.setEstado(EstadoSesion.CONFIRMED);

        SesionRequestDTO dto = new SesionRequestDTO(2L, 1L, manana9am.plusMinutes(30), 60, Modalidad.VIRTUAL);
        when(usuarioRepository.findByEmail("estudiante@ucr.ac.cr")).thenReturn(java.util.Optional.of(estudiante));
        when(usuarioRepository.findById(2L)).thenReturn(java.util.Optional.of(tutor));
        when(materiaRepository.findById(1L)).thenReturn(java.util.Optional.of(materia));
        when(disponibilidadService.cubreHorario(anyLong(), any(), anyInt())).thenReturn(true);
        when(sesionRepository.buscarDelUsuarioEnRango(eq(2L), eq(EstadoSesion.CONFIRMED), any(), any()))
                .thenReturn(List.of(existente));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> sesionService.reservar(dto, "estudiante@ucr.ac.cr"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(sesionRepository, never()).save(any());
    }

    @Test
    void noSePuedeCancelarUnaSesionConfirmadaConMenosDeDosHoras() {
        SesionTutoria sesion = new SesionTutoria(estudiante, tutor, materia,
                LocalDateTime.now().plusHours(1), 60, Modalidad.VIRTUAL);
        sesion.setEstado(EstadoSesion.CONFIRMED);
        when(sesionRepository.buscarConDetalle(1L)).thenReturn(java.util.Optional.of(sesion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> sesionService.cancelar(1L, "estudiante@ucr.ac.cr"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(EstadoSesion.CONFIRMED, sesion.getEstado());
    }

    @Test
    void noSePuedeIniciarUnaSesionAntesDeLaHoraPactada() {
        SesionTutoria sesion = new SesionTutoria(estudiante, tutor, materia,
                LocalDateTime.now().plusHours(3), 60, Modalidad.VIRTUAL);
        sesion.setEstado(EstadoSesion.CONFIRMED);
        when(sesionRepository.buscarConDetalle(1L)).thenReturn(java.util.Optional.of(sesion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> sesionService.iniciar(1L, "tutor@ucr.ac.cr"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(EstadoSesion.CONFIRMED, sesion.getEstado());
    }

    private Usuario crearUsuario(Long id, String email, Rol rol) {
        String nombre = rol == Rol.TUTOR ? "Mario" : "Sofía";
        String apellido = rol == Rol.TUTOR ? "Solano" : "Araya";
        Usuario usuario = new Usuario("X" + id, email, "hash", nombre, apellido, rol);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }
}