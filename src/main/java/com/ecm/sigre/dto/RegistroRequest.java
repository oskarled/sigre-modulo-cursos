package com.ecm.sigre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para que un participante cree su cuenta (rol ASISTENTE).
 *
 * @param nombre     nombre completo
 * @param correo     correo, sera el usuario para iniciar sesion
 * @param contrasena minimo 8 caracteres, con al menos una letra y un numero
 * @param documento  numero de documento de identidad (opcional)
 * @param telefono   telefono de contacto (opcional)
 */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite maximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 120, message = "El correo admite maximo 120 caracteres")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "La contrasena debe tener entre 8 y 64 caracteres, con al menos una letra y un numero")
        String contrasena,

        @Pattern(regexp = "^\\d{5,20}$", message = "El documento solo admite numeros (5 a 20 digitos)")
        String documento,

        @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "El telefono solo admite numeros, espacios y +")
        String telefono) {
}
