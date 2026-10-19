package com.ecm.sigre.common.exception;

/**
 * Se lanza cuando se busca un recurso (curso, usuario, inscripcion...) que no existe.
 * Se traduce en una respuesta HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje descriptivo.
     *
     * @param mensaje explicacion de lo que no se encontro
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
