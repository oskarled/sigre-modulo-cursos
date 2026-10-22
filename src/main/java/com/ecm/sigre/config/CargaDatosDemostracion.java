package com.ecm.sigre.config;

import com.ecm.sigre.model.*;
import com.ecm.sigre.repository.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea los roles y, si la base de datos esta vacia, los datos de demostracion
 * (los mismos usuarios que usa el frontend). Contrasena de todos: Sigre2026
 */
@Component
public class CargaDatosDemostracion implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaDatosDemostracion.class);
    public static final String CONTRASENA_DEMO = "Sigre2026";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstructorRepository instructorRepository;
    private final CursoEventoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CertificadoRepository certificadoRepository;
    private final PasswordEncoder passwordEncoder;

    public CargaDatosDemostracion(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                                  InstructorRepository instructorRepository, CursoEventoRepository cursoRepository,
                                  InscripcionRepository inscripcionRepository,
                                  CertificadoRepository certificadoRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.instructorRepository = instructorRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.certificadoRepository = certificadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (NombreRol nombre : NombreRol.values()) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                rolRepository.save(new Rol(nombre));
            }
        }
        if (usuarioRepository.count() > 0) {
            return;
        }

        // Usuarios (ids 1, 2 y 3 iguales a los del frontend)
        Usuario laura = usuario("Laura Mendoza", "laura.mendoza@ecm.com", NombreRol.ADMIN, "1047000001");
        Usuario carlos = usuario("Carlos Ortega", "carlos.ortega@ecm.com", NombreRol.INSTRUCTOR, "1047000002");
        Usuario maria = usuario("Maria Perez", "maria.perez@correo.com", NombreRol.ASISTENTE, "1047000003");
        Usuario pedro = usuario("Pedro Martinez", "pedro.martinez@ecm.com", NombreRol.INSTRUCTOR, "1047000004");
        Usuario juan = usuario("Juan Gomez", "juan.gomez@correo.com", NombreRol.ASISTENTE, "1047000005");
        Usuario ana = usuario("Ana Torres", "ana.torres@correo.com", NombreRol.ASISTENTE, "1047000006");

        Instructor iCarlos = instructorRepository.save(new Instructor(carlos, "Trabajo en alturas", "SST-2019-0451"));
        Instructor iPedro = instructorRepository.save(new Instructor(pedro, "Seguridad industrial", "SST-2020-1187"));

        // Fechas relativas a hoy para que las reglas (inicio futuro, 24 horas) funcionen siempre
        LocalDateTime hoy = LocalDateTime.now().with(LocalTime.of(8, 0));
        CursoEvento alturas1 = curso("Trabajo en Alturas Nivel Avanzado", 40, hoy.plusDays(20), 5,
                "Aula 301 - Sede Principal, Cartagena", 25, iCarlos);
        CursoEvento seguridad = curso("Seguridad Industrial Basica", 32, hoy.plusDays(30), 4,
                "Aula Virtual", 30, iPedro);
        CursoEvento auxilios = curso("Primeros Auxilios", 24, hoy.minusDays(40), 3,
                "Aula 205 - Sede Principal, Cartagena", 20, iCarlos);
        auxilios.setEstado(EstadoCurso.FINALIZADO);
        cursoRepository.save(auxilios);
        CursoEvento espacios = curso("Espacios Confinados", 16, hoy.plusDays(45), 2,
                "Campo de entrenamiento, Mamonal", 2, iPedro);

        // Inscripciones
        inscripcion(maria, alturas1, EstadoInscripcion.PENDIENTE);
        inscripcion(juan, alturas1, EstadoInscripcion.PAGADO);
        inscripcion(maria, seguridad, EstadoInscripcion.PENDIENTE);
        Inscripcion aprobada = inscripcion(maria, auxilios, EstadoInscripcion.APROBADO);
        inscripcion(juan, auxilios, EstadoInscripcion.NO_APROBADO);
        inscripcion(juan, espacios, EstadoInscripcion.PENDIENTE);
        inscripcion(ana, espacios, EstadoInscripcion.PAGADO);   // curso lleno: sirve para probar "sin cupos"

        certificadoRepository.save(new Certificado(aprobada, "ECM-2026-DEMO0001"));
        LOG.info("SIGRE: datos de demostracion cargados (contrasena de todos los usuarios: {})", CONTRASENA_DEMO);
    }

    private Usuario usuario(String nombre, String correo, NombreRol rol, String documento) {
        Usuario u = new Usuario(nombre, correo, passwordEncoder.encode(CONTRASENA_DEMO),
                rolRepository.findByNombre(rol).orElseThrow());
        u.setDocumento(documento);
        u.setTelefono("300 000 0000");
        return usuarioRepository.save(u);
    }

    private CursoEvento curso(String nombre, int horas, LocalDateTime inicio, int dias, String lugar, int cupo,
                              Instructor instructor) {
        return cursoRepository.save(new CursoEvento(nombre, horas, inicio,
                inicio.plusDays(dias - 1L).withHour(17), lugar, cupo, instructor));
    }

    private Inscripcion inscripcion(Usuario usuario, CursoEvento curso, EstadoInscripcion estado) {
        Inscripcion i = new Inscripcion(usuario, curso);
        i.setEstado(estado);
        return inscripcionRepository.save(i);
    }
}
