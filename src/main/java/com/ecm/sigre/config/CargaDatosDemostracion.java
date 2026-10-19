package com.ecm.sigre.config;

import com.ecm.sigre.usuarios.model.Instructor;
import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.model.TipoDocumento;
import com.ecm.sigre.usuarios.model.Usuario;
import com.ecm.sigre.usuarios.repository.InstructorRepository;
import com.ecm.sigre.usuarios.repository.RolRepository;
import com.ecm.sigre.usuarios.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga datos de demostracion (un administrador, un instructor y dos asistentes) para
 * poder probar el modulo sin tener aun el modulo de registro de usuarios.
 *
 * <p>Solo se ejecuta cuando la propiedad {@code sigre.datos-demo} es {@code true}
 * (el perfil {@code dev} la activa) y la tabla de usuarios esta vacia. Las contrasenas
 * se guardan cifradas con BCrypt.</p>
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "sigre.datos-demo", havingValue = "true")
public class CargaDatosDemostracion implements ApplicationRunner {

    private static final String CLAVE_DEMO = "Sigre2026*";

    private final RolRepository rolRepositorio;
    private final UsuarioRepository usuarioRepositorio;
    private final InstructorRepository instructorRepositorio;

    /**
     * Crea el cargador con los repositorios necesarios.
     *
     * @param rolRepositorio        repositorio de roles
     * @param usuarioRepositorio    repositorio de usuarios
     * @param instructorRepositorio repositorio de instructores
     */
    public CargaDatosDemostracion(RolRepository rolRepositorio, UsuarioRepository usuarioRepositorio,
                                  InstructorRepository instructorRepositorio) {
        this.rolRepositorio = rolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.instructorRepositorio = instructorRepositorio;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.count() > 0) {
            return;
        }
        String hash = new BCryptPasswordEncoder().encode(CLAVE_DEMO);

        usuarioRepositorio.save(new Usuario(TipoDocumento.CC, "1000000001", "Laura", "Mendoza",
                "admin@ecm.com", hash, buscarRol("ADMIN")));

        Usuario usuarioInstructor = usuarioRepositorio.save(new Usuario(TipoDocumento.CC, "1000000002",
                "Carlos", "Ortega", "instructor@ecm.com", hash, buscarRol("INSTRUCTOR")));
        instructorRepositorio.save(new Instructor(usuarioInstructor, "ALTURAS", "SST-ALT-0001",
                LocalDate.now().plusYears(1)));

        usuarioRepositorio.save(new Usuario(TipoDocumento.CC, "1000000003", "Maria", "Perez",
                "maria.perez@correo.com", hash, buscarRol(Rol.ASISTENTE)));
        usuarioRepositorio.save(new Usuario(TipoDocumento.CC, "1000000004", "Juan", "Gomez",
                "juan.gomez@correo.com", hash, buscarRol(Rol.ASISTENTE)));
    }

    /** Busca un rol ya cargado por {@link CargaRolesIniciales}. */
    private Rol buscarRol(String nombre) {
        return rolRepositorio.findByNombre(nombre).orElseThrow();
    }
}
