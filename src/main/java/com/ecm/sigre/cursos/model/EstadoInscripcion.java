package com.ecm.sigre.cursos.model;

/**
 * Estados de una inscripcion (dominio de la columna estado_inscripcion).
 */
public enum EstadoInscripcion {
    /** Inscripcion registrada, pendiente de pago. */
    PENDIENTE,
    /** Pago verificado. */
    PAGADO,
    /** Curso aprobado por el participante. */
    APROBADO,
    /** Curso no aprobado. */
    NO_APROBADO,
    /** Inscripcion cancelada; libera el cupo. */
    CANCELADO
}
