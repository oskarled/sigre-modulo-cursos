package com.ecm.sigre.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operacion incumple una regla de negocio del SIGRE
 * (por ejemplo, cupo agotado o instructor con licencia vencida).
 *
 * <p>Lleva el codigo HTTP con el que se debe responder: 400 cuando los datos
 * enviados son incoherentes y 409 cuando la operacion choca con el estado actual.</p>
 */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estado;

    /**
     * Crea la excepcion con su codigo HTTP y un mensaje descriptivo.
     *
     * @param estado  codigo HTTP de la respuesta
     * @param mensaje explicacion de la regla incumplida
     */
    public ReglaNegocioException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    /**
     * Devuelve el codigo HTTP asociado a la regla incumplida.
     *
     * @return codigo HTTP
     */
    public HttpStatus getEstado() {
        return estado;
    }
}
