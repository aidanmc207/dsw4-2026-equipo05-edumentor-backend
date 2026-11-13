package cr.ac.ucr.edumentor.service;

import cr.ac.ucr.edumentor.dto.MateriaRequestDTO;
import cr.ac.ucr.edumentor.dto.MateriaResponseDTO;
import cr.ac.ucr.edumentor.entity.Materia;
import cr.ac.ucr.edumentor.repository.MateriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private MateriaService materiaService;

    @Test
    void crearGuardaMateriaConCodigoEnMayuscula() {
        MateriaRequestDTO dto = new MateriaRequestDTO(" if2000 ", "Programación I", "Informática Empresarial", 4);
        when(materiaRepository.existsByCodigoMateria("IF2000")).thenReturn(false);
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        MateriaResponseDTO creada = materiaService.crear(dto);

        assertEquals("IF2000", creada.codigoMateria());
        assertEquals("Programación I", creada.nombreMateria());
    }

    @Test
    void crearRechazaCodigoRepetido() {
        MateriaRequestDTO dto = new MateriaRequestDTO("IF2000", "Programación I", "Informática Empresarial", 4);
        when(materiaRepository.existsByCodigoMateria("IF2000")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> materiaService.crear(dto));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(materiaRepository, never()).save(any());
    }

    @Test
    void obtenerMateriaInexistenteDevuelve404() {
        when(materiaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> materiaService.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
