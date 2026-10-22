package com.ecm.sigre.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo para cambiar el estado de un curso o de una inscripcion.
 *
 * @param estado nuevo estado, por ejemplo "PAGADO" o "EN_CURSO"
 */
public record CambioEstadoRequest(
        @NotBlank(message = "El estado es obligatorio")
        String estado) {
}
