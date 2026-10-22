package com.ecm.sigre.service;

import com.ecm.sigre.dto.LoginRequest;
import com.ecm.sigre.dto.LoginResponse;
import com.ecm.sigre.dto.RegistroRequest;
import com.ecm.sigre.dto.UsuarioResponse;
import com.ecm.sigre.exception.AutenticacionException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.Instructor;
import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Usuario;
import com.ecm.sigre.repository.InstructorRepository;
import com.ecm.sigre.repository.RolRepository;
import com.ecm.sigre.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro de participantes e inicio de sesion. */
@Service
public class AuthService {

    /** Mensaje unico para no revelar si el correo existe o no. */
    private static final String ERROR_CREDENCIALES = "Error en la autenticacion: correo o contrasena incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       InstructorRepository instructorRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea la cuenta de un participante con rol ASISTENTE.
     *
     * @throws ReglaNegocioException si el correo ya esta registrado
     */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el correo " + correo);
        }
        Usuario usuario = new Usuario(datos.nombre().trim(), correo,
                passwordEncoder.encode(datos.contrasena()),
                rolRepository.findByNombre(NombreRol.ASISTENTE).orElseThrow());
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /**
     * Valida el correo y la contrasena.
     *
     * @return datos del usuario y mensaje "Autenticacion satisfactoria"
     * @throws AutenticacionException si las credenciales no son correctas o el usuario esta inactivo
     */
    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(datos.correo().trim())
                .filter(u -> passwordEncoder.matches(datos.contrasena(), u.getContrasenaHash()))
                .orElseThrow(() -> new AutenticacionException(ERROR_CREDENCIALES));
        if (!usuario.isActivo()) {
            throw new AutenticacionException("Error en la autenticacion: el usuario esta inactivo");
        }
        Long idInstructor = instructorRepository.findByUsuarioId(usuario.getId())
                .map(Instructor::getId)
                .orElse(null);
        return new LoginResponse("Autenticacion satisfactoria", usuario.getId(), usuario.getNombre(),
                usuario.getCorreo(), usuario.getRol().getNombre().name(), idInstructor);
    }
}
