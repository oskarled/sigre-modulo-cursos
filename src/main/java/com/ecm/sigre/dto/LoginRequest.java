package com.ecm.sigre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos para iniciar sesion.
 *
 * @param correo     correo registrado del usuario
 * @param contrasena contrasena en texto plano (viaja solo en la peticion, nunca se guarda asi)
 */
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        String contrasena) {
}
