package com.ecm.sigre.exception;

/** Se lanza cuando un dato de la peticion no es valido. Responde 400. */
public class DatosInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
