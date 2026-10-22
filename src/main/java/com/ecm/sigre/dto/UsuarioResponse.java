package com.ecm.sigre.dto;

import com.ecm.sigre.model.Usuario;
import java.time.LocalDateTime;

/** Usuario sin la contrasena. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String documento,
        String telefono,
        String rol,
        boolean activo,
        LocalDateTime fechaRegistro) {

    /** Convierte la entidad en la respuesta de la API. */
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getDocumento(),
                u.getTelefono(), u.getRol().getNombre().name(), u.isActivo(), u.getFechaRegistro());
    }
}
