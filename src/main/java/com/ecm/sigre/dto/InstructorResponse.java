package com.ecm.sigre.dto;

import com.ecm.sigre.model.Instructor;

/** Instructor con los datos de su usuario. */
public record InstructorResponse(
        Long id,
        Long idUsuario,
        String nombre,
        String correo,
        String telefono,
        String especialidad,
        String licenciaSst) {

    /** Convierte la entidad en la respuesta de la API. */
    public static InstructorResponse de(Instructor i) {
        return new InstructorResponse(i.getId(), i.getUsuario().getId(), i.getUsuario().getNombre(),
                i.getUsuario().getCorreo(), i.getUsuario().getTelefono(), i.getEspecialidad(), i.getLicenciaSst());
    }
}
