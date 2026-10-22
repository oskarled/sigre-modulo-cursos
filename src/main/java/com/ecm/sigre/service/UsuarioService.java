package com.ecm.sigre.service;

import com.ecm.sigre.dto.UsuarioRequest;
import com.ecm.sigre.dto.UsuarioResponse;
import com.ecm.sigre.exception.DatosInvalidosException;
import com.ecm.sigre.exception.RecursoNoEncontradoException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Usuario;
import com.ecm.sigre.repository.InscripcionRepository;
import com.ecm.sigre.repository.InstructorRepository;
import com.ecm.sigre.repository.RolRepository;
import com.ecm.sigre.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de usuarios (lo usa el administrador). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InscripcionRepository inscripcionRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          InscripcionRepository inscripcionRepository, InstructorRepository instructorRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lista los usuarios, opcionalmente filtrados por rol.
     *
     * @param rol ASISTENTE, INSTRUCTOR, ADMIN o null para todos
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(String rol) {
        List<Usuario> usuarios = (rol == null || rol.isBlank())
                ? usuarioRepository.findAllByOrderByNombre()
                : usuarioRepository.findByRolNombreOrderByNombre(Enums.convertir(NombreRol.class, rol, "rol"));
        return usuarios.stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    /** Crea un usuario. La contrasena es obligatoria. */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest datos) {
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new DatosInvalidosException("La contrasena es obligatoria al crear un usuario");
        }
        String correo = datos.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo " + correo);
        }
        Usuario usuario = new Usuario(datos.nombre().trim(), correo,
                passwordEncoder.encode(datos.contrasena()), rol(datos.rol()));
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        if (datos.activo() != null) {
            usuario.setActivo(datos.activo());
        }
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /** Actualiza un usuario. Si la contrasena viene vacia se conserva la actual. */
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest datos) {
        Usuario usuario = buscar(id);
        String correo = datos.correo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe otro usuario con el correo " + correo);
                });
        NombreRol nuevoRol = Enums.convertir(NombreRol.class, datos.rol(), "rol");
        if (nuevoRol != NombreRol.INSTRUCTOR && instructorRepository.findByUsuarioId(id).isPresent()) {
            throw new ReglaNegocioException("El usuario es instructor; eliminelo primero como instructor para cambiar su rol");
        }
        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        usuario.setRol(rol(datos.rol()));
        if (datos.activo() != null) {
            usuario.setActivo(datos.activo());
        }
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(datos.contrasena()));
        }
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /**
     * Elimina un usuario que no tenga historial.
     *
     * @throws ReglaNegocioException si tiene inscripciones o es instructor (se debe desactivar en su lugar)
     */
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (inscripcionRepository.existsByUsuarioId(id)) {
            throw new ReglaNegocioException(
                    "El usuario tiene inscripciones registradas; desactivelo (activo=false) en lugar de eliminarlo");
        }
        if (instructorRepository.findByUsuarioId(id).isPresent()) {
            throw new ReglaNegocioException("El usuario es instructor; eliminelo desde /api/instructores");
        }
        usuarioRepository.delete(usuario);
    }

    Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private com.ecm.sigre.model.Rol rol(String nombre) {
        NombreRol rol = Enums.convertir(NombreRol.class, nombre, "rol");
        return rolRepository.findByNombre(rol).orElseThrow();
    }
}
