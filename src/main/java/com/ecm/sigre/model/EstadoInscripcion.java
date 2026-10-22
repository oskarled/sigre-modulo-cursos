package com.ecm.sigre.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de una inscripcion y las transiciones permitidas entre ellos.
 * PENDIENTE -> PAGADO -> APROBADO | NO_APROBADO. Cualquier estado activo puede pasar a CANCELADO.
 */
public enum EstadoInscripcion {
    PENDIENTE,
    PAGADO,
    APROBADO,
    NO_APROBADO,
    CANCELADO;

    /**
     * Indica si se puede pasar de este estado al estado indicado.
     *
     * @param nuevo estado destino
     * @return true si la transicion es valida
     */
    public boolean puedeCambiarA(EstadoInscripcion nuevo) {
        Set<EstadoInscripcion> permitidos = switch (this) {
            case PENDIENTE -> EnumSet.of(PAGADO, CANCELADO);
            case PAGADO -> EnumSet.of(APROBADO, NO_APROBADO, CANCELADO);
            case APROBADO, NO_APROBADO, CANCELADO -> EnumSet.noneOf(EstadoInscripcion.class);
        };
        return permitidos.contains(nuevo);
    }
}
