package com.ecm.sigre.cursos.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud de inscripcion a un curso (HU-09).
 *
 * <p>Mientras el modulo de autenticacion no este integrado, el participante se indica
 * por su id; despues se tomara del token JWT de la sesion.</p>
 *
 * @param idUsuario identificador del usuario participante
 */
public record InscripcionRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long idUsuario
) {
}
