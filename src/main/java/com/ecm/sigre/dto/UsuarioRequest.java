package com.ecm.sigre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para que el administrador cree o actualice un usuario.
 * En la actualizacion la contrasena es opcional: si viene vacia se conserva la anterior.
 *
 * @param nombre     nombre completo
 * @param correo     correo unico
 * @param contrasena contrasena (obligatoria solo al crear)
 * @param documento  documento de identidad (opcional)
 * @param telefono   telefono (opcional)
 * @param rol        ASISTENTE, INSTRUCTOR o ADMIN
 * @param activo     si el usuario puede iniciar sesion (opcional, por defecto true)
 */
public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite maximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "La contrasena debe tener entre 8 y 64 caracteres, con al menos una letra y un numero")
        String contrasena,

        @Pattern(regexp = "^\\d{5,20}$", message = "El documento solo admite numeros (5 a 20 digitos)")
        String documento,

        @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "El telefono solo admite numeros, espacios y +")
        String telefono,

        @NotNull(message = "El rol es obligatorio (ASISTENTE, INSTRUCTOR o ADMIN)")
        String rol,

        Boolean activo) {
}
