package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.EvaluacionRequestDTO;
import cr.ac.ucr.edumentor.dto.EvaluacionResponseDTO;
import cr.ac.ucr.edumentor.entity.EstadoSesion;
import cr.ac.ucr.edumentor.entity.EvaluacionTutoria;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.entity.Modalidad;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.EvaluacionTutoriaRepository;
import cr.ac.ucr.edumentor.repository.SesionRepository;
import cr.ac.ucr.edumentor.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionServiceTest {

    private static final String EMAIL_ESTUDIANTE = "estudiante@ucr.ac.cr";

    @Mock
    private EvaluacionTutoriaRepository evaluacionRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EvaluacionService evaluacionService;

    private Usuario tutor;
    private SesionTutoria sesion;

    @BeforeEach
    void preparar() {
        Usuario estudiante = new Usuario("B00001", EMAIL_ESTUDIANTE, "hash", "Ana", "Mora", Rol.ESTUDIANTE);
        tutor = new Usuario("B00002", "tutor@ucr.ac.cr", "hash", "Luis", "Rojas", Rol.TUTOR);
        Materia materia = new Materia("IF2000", "Programación I", "Informática Empresarial", 4);
        sesion = new SesionTutoria(estudiante, tutor, materia, LocalDateTime.now().minusDays(1), 60, Modalidad.VIRTUAL);
        sesion.setEstado(EstadoSesion.COMPLETED);
    }

    @Test
    void crearGuardaLaEvaluacionYRecalculaElPromedioDelTutor() {
        EvaluacionRequestDTO dto = new EvaluacionRequestDTO(4, "Explica muy bien");
        List<EvaluacionTutoria> evaluacionesDelTutor = List.of(
                new EvaluacionTutoria(sesion, 5, null),
                new EvaluacionTutoria(sesion, 4, null),
                new EvaluacionTutoria(sesion, 4, null));
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(evaluacionRepository.save(any(EvaluacionTutoria.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(evaluacionRepository.findBySesionTutorIdOrderByFechaEvaluacionDesc(any())).thenReturn(evaluacionesDelTutor);

        EvaluacionResponseDTO creada = evaluacionService.crear(1L, dto, EMAIL_ESTUDIANTE);

        assertEquals(4, creada.calificacionEstrellas());
        assertEquals(EstadoSesion.REVIEWED, sesion.getEstado());
        assertEquals(4.33, tutor.getPromedioCalificacion(), 0.001);
        assertEquals(3, tutor.getTotalEvaluaciones());
    }

    @Test
    void crearRechazaUnaSesionQueNoEstaCompletada() {
        sesion.setEstado(EstadoSesion.CONFIRMED);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> evaluacionService.crear(1L, new EvaluacionRequestDTO(5, null), EMAIL_ESTUDIANTE));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void crearRechazaAUnEstudianteQueNoEsDeLaSesion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> evaluacionService.crear(1L, new EvaluacionRequestDTO(5, null), "otro@ucr.ac.cr"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void crearRechazaUnaSesionQueYaFueEvaluada() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(evaluacionRepository.existsBySesionId(1L)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> evaluacionService.crear(1L, new EvaluacionRequestDTO(5, null), EMAIL_ESTUDIANTE));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(evaluacionRepository, never()).save(any());
    }
}
