package com.ecm.sigre.cursos.model;

/**
 * Estados de una sesion de curso (dominio de la columna estado de curso_evento).
 */
public enum EstadoCurso {
    /** Curso programado, abierto a inscripciones. */
    PROGRAMADO,
    /** Curso en ejecucion. */
    EN_CURSO,
    /** Curso terminado. */
    FINALIZADO,
    /** Curso cancelado o deshabilitado. */
    CANCELADO
}
