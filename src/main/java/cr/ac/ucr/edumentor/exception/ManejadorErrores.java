package cr.ac.ucr.edumentor.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    private record ErrorCampo(String campo, String mensaje) {
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorCampo(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problema = ex.getBody();
        problema.setDetail("Hay campos con datos inválidos.");
        problema.setProperty("errores", errores);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> respuesta = super.handleExceptionInternal(ex, body, headers, status, request);
        if (respuesta != null && respuesta.getBody() instanceof ProblemDetail problema) {
            problema.setTitle(titulo(status.value()));
        }
        return respuesta;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado, intente de nuevo.");
        problema.setTitle(titulo(500));
        return problema;
    }

    private String titulo(int codigo) {
        return switch (codigo) {
            case 400 -> "Solicitud inválida";
            case 401 -> "No autenticado";
            case 403 -> "Sin permiso";
            case 404 -> "No encontrado";
            case 405 -> "Método no permitido";
            case 409 -> "Conflicto";
            case 500 -> "Error del servidor";
            default -> "Error";
        };
    }
}
