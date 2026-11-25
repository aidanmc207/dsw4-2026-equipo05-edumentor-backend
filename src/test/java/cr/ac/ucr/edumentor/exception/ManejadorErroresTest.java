package cr.ac.ucr.edumentor.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ManejadorErroresTest {

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void responseStatusExceptionSeDevuelveComoProblemDetails() throws Exception {
        mockMvc.perform(get("/prueba/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("No encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe el recurso"));
    }

    @Test
    void validacionFallidaIncluyeLosErroresPorCampo() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"))
                .andExpect(jsonPath("$.errores[0].campo").value("titulo"));
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorDePrueba {

        @GetMapping("/no-existe")
        void noExiste() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el recurso");
        }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody PeticionPrueba peticion) {
        }
    }

    record PeticionPrueba(@NotBlank(message = "El título es obligatorio") String titulo) {
    }
}
