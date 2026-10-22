package com.ecm.sigre.exception;

/** Se lanza cuando no existe el registro pedido. Responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
