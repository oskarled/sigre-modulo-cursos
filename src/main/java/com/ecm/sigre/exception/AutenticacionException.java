package com.ecm.sigre.exception;

/** Se lanza cuando el correo o la contrasena no son correctos. Responde 401. */
public class AutenticacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
