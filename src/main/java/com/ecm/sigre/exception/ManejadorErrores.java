package com.ecm.sigre.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte las excepciones en respuestas JSON con el formato ProblemDetail (RFC 7807):
 * { "type", "title", "status", "detail", "instance", "errores"? }.
 * El frontend muestra el campo "detail" al usuario.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio", ex.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ProblemDetail datosInvalidos(DatosInvalidosException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Datos invalidos", ex.getMessage());
    }

    @ExceptionHandler(AutenticacionException.class)
    public ProblemDetail autenticacion(AutenticacionException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Error en la autenticacion", ex.getMessage());
    }

    /** Errores de @Valid: devuelve un mapa campo -> mensaje en "errores". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Datos invalidos",
                "La peticion tiene datos invalidos");
        p.setProperty("errores", errores);
        return p;
    }

    /** JSON mal escrito o con tipos equivocados (por ejemplo una fecha con formato incorrecto). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Datos invalidos",
                "El cuerpo de la peticion no es un JSON valido o tiene campos con formato incorrecto "
                        + "(las fechas usan AAAA-MM-DDTHH:mm:ss)");
    }

    /** Un id en la URL que no es numerico, por ejemplo /api/cursos/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Datos invalidos",
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue());
    }

    private ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(estado, detalle);
        p.setTitle(titulo);
        return p;
    }
}
