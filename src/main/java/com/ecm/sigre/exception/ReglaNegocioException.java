package com.ecm.sigre.exception;

/** Se lanza cuando la peticion choca con una regla del negocio (cupos, fechas, duplicados). Responde 409. */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
