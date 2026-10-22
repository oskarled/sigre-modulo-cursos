package com.ecm.sigre.dto;

/**
 * Respuesta de un inicio de sesion correcto.
 *
 * @param mensaje      "Autenticacion satisfactoria"
 * @param idUsuario    id del usuario
 * @param nombre       nombre completo
 * @param correo       correo
 * @param rol          ASISTENTE, INSTRUCTOR o ADMIN
 * @param idInstructor id en la tabla instructor (solo si el rol es INSTRUCTOR)
 */
public record LoginResponse(
        String mensaje,
        Long idUsuario,
        String nombre,
        String correo,
        String rol,
        Long idInstructor) {
}
