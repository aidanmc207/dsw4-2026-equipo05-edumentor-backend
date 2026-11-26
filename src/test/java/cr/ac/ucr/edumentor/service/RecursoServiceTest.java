package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.RecursoRequestDTO;
import cr.ac.ucr.edumentor.dto.RecursoResponseDTO;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.entity.Modalidad;
import cr.ac.ucr.edumentor.entity.RecursoEstudio;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.SesionTutoria;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.RecursoEstudioRepository;
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
class RecursoServiceTest {

    private static final String EMAIL_TUTOR = "tutor@ucr.ac.cr";
    private static final String EMAIL_ESTUDIANTE = "estudiante@ucr.ac.cr";

    @Mock
    private RecursoEstudioRepository recursoRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RecursoService recursoService;

    private SesionTutoria sesion;

    @BeforeEach
    void preparar() {
        Usuario estudiante = new Usuario("B00001", EMAIL_ESTUDIANTE, "hash", "Ana", "Mora", Rol.ESTUDIANTE);
        Usuario tutor = new Usuario("B00002", EMAIL_TUTOR, "hash", "Luis", "Rojas", Rol.TUTOR);
        Materia materia = new Materia("IF2000", "Programación I", "Informática Empresarial", 4);
        sesion = new SesionTutoria(estudiante, tutor, materia, LocalDateTime.now().plusDays(1), 60, Modalidad.VIRTUAL);
    }

    @Test
    void crearGuardaElEnlaceSiLoAgregaElTutorDeLaSesion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(recursoRepository.save(any(RecursoEstudio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        RecursoResponseDTO creado = recursoService.crear(1L,
                new RecursoRequestDTO("  Guía de ciclos ", "https://ejemplo.com/guia"), EMAIL_TUTOR);

        assertEquals("Guía de ciclos", creado.titulo());
        assertEquals("https://ejemplo.com/guia", creado.urlRecurso());
    }

    @Test
    void crearRechazaAUnUsuarioQueNoEsElTutorDeLaSesion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recursoService.crear(1L, new RecursoRequestDTO("Guía", "https://ejemplo.com"), EMAIL_ESTUDIANTE));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(recursoRepository, never()).save(any());
    }

    @Test
    void listarRechazaAUnEstudianteQueNoEsDeLaSesion() {
        Usuario otro = new Usuario("B00003", "otro@ucr.ac.cr", "hash", "Eva", "Solís", Rol.ESTUDIANTE);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(usuarioRepository.findByEmail("otro@ucr.ac.cr")).thenReturn(Optional.of(otro));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recursoService.listarDeSesion(1L, "otro@ucr.ac.cr"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void listarDevuelveLosRecursosAlEstudianteDeLaSesion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesion));
        when(recursoRepository.findBySesionIdOrderByIdAsc(1L))
                .thenReturn(List.of(new RecursoEstudio(sesion, "Guía", "https://ejemplo.com")));

        List<RecursoResponseDTO> recursos = recursoService.listarDeSesion(1L, EMAIL_ESTUDIANTE);

        assertEquals(1, recursos.size());
    }

    @Test
    void eliminarRechazaAUnUsuarioQueNoEsElTutorDeLaSesion() {
        RecursoEstudio recurso = new RecursoEstudio(sesion, "Guía", "https://ejemplo.com");
        when(recursoRepository.findById(5L)).thenReturn(Optional.of(recurso));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> recursoService.eliminar(5L, EMAIL_ESTUDIANTE));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(recursoRepository, never()).delete(any());
    }
}
