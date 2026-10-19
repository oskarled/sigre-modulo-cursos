package com.ecm.sigre.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la API (patron descrito en la arquitectura del SIGRE).
 *
 * <p>Convierte las excepciones en respuestas HTTP con formato {@link ProblemDetail}
 * (RFC 7807), de modo que el cliente reciba mensajes claros y nunca trazas internas
 * del servidor.</p>
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    /**
     * Maneja los recursos que no existen.
     *
     * @param ex excepcion lanzada por la capa de servicio
     * @return respuesta HTTP 404
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail detalle = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detalle.setTitle("Recurso no encontrado");
        return detalle;
    }

    /**
     * Maneja las reglas de negocio incumplidas.
     *
     * @param ex excepcion con el codigo HTTP correspondiente
     * @return respuesta HTTP con el codigo indicado por la regla (400 o 409)
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
        ProblemDetail detalle = ProblemDetail.forStatusAndDetail(ex.getEstado(), ex.getMessage());
        detalle.setTitle("Regla de negocio incumplida");
        return detalle;
    }

    /**
     * Maneja los datos de entrada que no cumplen las validaciones.
     *
     * @param ex excepcion con los errores de validacion por campo
     * @return respuesta HTTP 400 con el detalle de cada campo invalido
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail detalle = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Los datos enviados no son validos");
        detalle.setTitle("Error de validacion");
        detalle.setProperty("errores", errores);
        return detalle;
    }
}
