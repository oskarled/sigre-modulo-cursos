package com.ecm.sigre.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para inscribir un participante en un curso.
 *
 * @param idUsuario id del usuario con rol ASISTENTE
 */
public record InscripcionRequest(
        @NotNull(message = "El idUsuario es obligatorio")
        Long idUsuario) {
}
