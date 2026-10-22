package com.ecm.sigre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un instructor. Al crearlo tambien se crea su usuario con rol INSTRUCTOR.
 *
 * @param nombre       nombre completo
 * @param correo       correo unico
 * @param contrasena   contrasena (obligatoria solo al crear)
 * @param telefono     telefono (opcional)
 * @param especialidad area que dicta, por ejemplo "Trabajo en alturas"
 * @param licenciaSst  numero de licencia SST (opcional)
 */
public record InstructorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite maximo 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "La contrasena debe tener entre 8 y 64 caracteres, con al menos una letra y un numero")
        String contrasena,

        @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "El telefono solo admite numeros, espacios y +")
        String telefono,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad admite maximo 100 caracteres")
        String especialidad,

        @Size(max = 40, message = "La licencia admite maximo 40 caracteres")
        String licenciaSst) {
}
