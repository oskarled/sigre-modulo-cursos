package com.ecm.sigre.service;

import com.ecm.sigre.dto.InstructorRequest;
import com.ecm.sigre.dto.InstructorResponse;
import com.ecm.sigre.exception.DatosInvalidosException;
import com.ecm.sigre.exception.RecursoNoEncontradoException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.Instructor;
import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Usuario;
import com.ecm.sigre.repository.CursoEventoRepository;
import com.ecm.sigre.repository.InstructorRepository;
import com.ecm.sigre.repository.RolRepository;
import com.ecm.sigre.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de instructores. Cada instructor tiene un usuario con rol INSTRUCTOR. */
@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CursoEventoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    public InstructorService(InstructorRepository instructorRepository, UsuarioRepository usuarioRepository,
                             RolRepository rolRepository, CursoEventoRepository cursoRepository,
                             PasswordEncoder passwordEncoder) {
        this.instructorRepository = instructorRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.cursoRepository = cursoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<InstructorResponse> listar() {
        return instructorRepository.findAllByOrderByUsuarioNombre().stream().map(InstructorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InstructorResponse obtener(Long id) {
        return InstructorResponse.de(buscar(id));
    }

    /** Crea el usuario (rol INSTRUCTOR) y el registro de instructor. */
    @Transactional
    public InstructorResponse crear(InstructorRequest datos) {
        if (datos.contrasena() == null || datos.contrasena().isBlank()) {
            throw new DatosInvalidosException("La contrasena es obligatoria al crear un instructor");
        }
        String correo = datos.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo " + correo);
        }
        Usuario usuario = new Usuario(datos.nombre().trim(), correo, passwordEncoder.encode(datos.contrasena()),
                rolRepository.findByNombre(NombreRol.INSTRUCTOR).orElseThrow());
        usuario.setTelefono(datos.telefono());
        usuarioRepository.save(usuario);
        Instructor instructor = new Instructor(usuario, datos.especialidad().trim(), datos.licenciaSst());
        return InstructorResponse.de(instructorRepository.save(instructor));
    }

    /** Actualiza los datos del instructor y de su usuario. */
    @Transactional
    public InstructorResponse actualizar(Long id, InstructorRequest datos) {
        Instructor instructor = buscar(id);
        Usuario usuario = instructor.getUsuario();
        String correo = datos.correo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe otro usuario con el correo " + correo);
                });
        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(datos.telefono());
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(datos.contrasena()));
        }
        instructor.setEspecialidad(datos.especialidad().trim());
        instructor.setLicenciaSst(datos.licenciaSst());
        return InstructorResponse.de(instructorRepository.save(instructor));
    }

    /**
     * Elimina el instructor y su usuario.
     *
     * @throws ReglaNegocioException si tiene cursos asignados
     */
    @Transactional
    public void eliminar(Long id) {
        Instructor instructor = buscar(id);
        if (cursoRepository.existsByInstructorId(id)) {
            throw new ReglaNegocioException("El instructor tiene cursos asignados; reasignelos antes de eliminarlo");
        }
        Usuario usuario = instructor.getUsuario();
        instructorRepository.delete(instructor);
        usuarioRepository.delete(usuario);
    }

    Instructor buscar(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el instructor con id " + id));
    }
}
