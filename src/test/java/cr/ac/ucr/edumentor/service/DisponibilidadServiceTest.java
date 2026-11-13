package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.DisponibilidadRequestDTO;
import cr.ac.ucr.edumentor.dto.DisponibilidadResponseDTO;
import cr.ac.ucr.edumentor.entity.DisponibilidadTutor;
import cr.ac.ucr.edumentor.entity.Rol;
import cr.ac.ucr.edumentor.entity.Usuario;
import cr.ac.ucr.edumentor.repository.DisponibilidadRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    private static final Long TUTOR_ID = 5L;

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DisponibilidadService disponibilidadService;

    private Usuario tutor;

    @BeforeEach
    void prepararTutor() {
        tutor = new Usuario("B12345", "tutor@ucr.ac.cr", "hash", "Laura", "Mora", Rol.TUTOR);
        ReflectionTestUtils.setField(tutor, "id", TUTOR_ID);
    }

    private DisponibilidadTutor bloque(Long id, DayOfWeek dia, int horaInicio, int horaFin) {
        DisponibilidadTutor bloque = new DisponibilidadTutor(tutor, dia, LocalTime.of(horaInicio, 0), LocalTime.of(horaFin, 0));
        ReflectionTestUtils.setField(bloque, "id", id);
        return bloque;
    }

    @Test
    void crearGuardaBloqueQueSoloTocaElAnterior() {
        when(usuarioRepository.findById(TUTOR_ID)).thenReturn(Optional.of(tutor));
        when(disponibilidadRepository.findByTutorIdAndDiaSemana(TUTOR_ID, DayOfWeek.MONDAY))
                .thenReturn(List.of(bloque(1L, DayOfWeek.MONDAY, 8, 10)));
        when(disponibilidadRepository.save(any(DisponibilidadTutor.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        DisponibilidadRequestDTO dto = new DisponibilidadRequestDTO(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), null);
        DisponibilidadResponseDTO creado = disponibilidadService.crear(TUTOR_ID, dto);

        assertEquals(LocalTime.of(10, 0), creado.horaInicio());
        assertTrue(creado.estaActivo());
    }

    @Test
    void crearRechazaBloqueQueSeTraslapa() {
        when(usuarioRepository.findById(TUTOR_ID)).thenReturn(Optional.of(tutor));
        when(disponibilidadRepository.findByTutorIdAndDiaSemana(TUTOR_ID, DayOfWeek.MONDAY))
                .thenReturn(List.of(bloque(1L, DayOfWeek.MONDAY, 8, 10)));

        DisponibilidadRequestDTO dto = new DisponibilidadRequestDTO(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> disponibilidadService.crear(TUTOR_ID, dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(disponibilidadRepository, never()).save(any());
    }

    @Test
    void crearRechazaHoraFinAntesDeInicio() {
        when(usuarioRepository.findById(TUTOR_ID)).thenReturn(Optional.of(tutor));

        DisponibilidadRequestDTO dto = new DisponibilidadRequestDTO(DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(13, 0), true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> disponibilidadService.crear(TUTOR_ID, dto));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void crearRechazaUsuarioQueNoEsTutor() {
        Usuario estudiante = new Usuario("C54321", "est@ucr.ac.cr", "hash", "Ana", "Solís", Rol.ESTUDIANTE);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(estudiante));

        DisponibilidadRequestDTO dto = new DisponibilidadRequestDTO(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0), true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> disponibilidadService.crear(7L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void actualizarNoChocaConsigoMismo() {
        DisponibilidadTutor existente = bloque(1L, DayOfWeek.MONDAY, 8, 10);
        when(disponibilidadRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByTutorIdAndDiaSemana(TUTOR_ID, DayOfWeek.MONDAY)).thenReturn(List.of(existente));

        DisponibilidadRequestDTO dto = new DisponibilidadRequestDTO(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(11, 0), null);
        DisponibilidadResponseDTO actualizado = disponibilidadService.actualizar(1L, dto);

        assertEquals(LocalTime.of(11, 0), actualizado.horaFin());
    }

    @Test
    void cubreHorarioSoloSiLaSesionCabeEnElBloque() {
        when(disponibilidadRepository.findByTutorIdAndDiaSemana(TUTOR_ID, DayOfWeek.MONDAY))
                .thenReturn(List.of(bloque(1L, DayOfWeek.MONDAY, 8, 10)));
        LocalDateTime lunes = LocalDateTime.of(2026, 11, 16, 9, 0);

        assertTrue(disponibilidadService.cubreHorario(TUTOR_ID, lunes, 60));
        assertFalse(disponibilidadService.cubreHorario(TUTOR_ID, lunes, 90));
    }
}
