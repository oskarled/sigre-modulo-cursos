package com.ecm.sigre.cursos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.model.Inscripcion;
import com.ecm.sigre.cursos.specification.CursoEventoEspecificaciones;
import com.ecm.sigre.usuarios.model.Instructor;
import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.model.TipoDocumento;
import com.ecm.sigre.usuarios.model.Usuario;
import com.ecm.sigre.usuarios.repository.InstructorRepository;
import com.ecm.sigre.usuarios.repository.RolRepository;
import com.ecm.sigre.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pruebas de los repositorios de cursos e inscripciones contra una base de datos H2
 * en memoria. Cada prueba se ejecuta en una transaccion que se revierte al terminar.
 */
@DataJpaTest
class CursoInscripcionRepositoryTest {

    @Autowired
    private CursoEventoRepository cursoRepositorio;

    @Autowired
    private InscripcionRepository inscripcionRepositorio;

    @Autowired
    private RolRepository rolRepositorio;

    @Autowired
    private UsuarioRepository usuarioRepositorio;

    @Autowired
    private InstructorRepository instructorRepositorio;

    @Autowired
    private TestEntityManager entityManager;

    private Rol rolAsistente;
    private Instructor instructor;

    /** Prepara el rol de asistente y un instructor con licencia vigente. */
    @BeforeEach
    void prepararDatosBase() {
        Rol rolInstructor = rolRepositorio.save(new Rol("INSTRUCTOR"));
        rolAsistente = rolRepositorio.save(new Rol(Rol.ASISTENTE));
        Usuario usuarioInstructor = usuarioRepositorio.save(new Usuario(TipoDocumento.CC, "2000000001",
                "Carlos", "Ortega", "instructor@ecm.com", "hash", rolInstructor));
        instructor = instructorRepositorio.save(new Instructor(usuarioInstructor, "ALTURAS", "SST-1",
                LocalDate.now().plusYears(1)));
    }

    private Usuario guardarAsistente(String documento) {
        return usuarioRepositorio.save(new Usuario(TipoDocumento.CC, documento, "Nombre", "Apellido",
                documento + "@correo.com", "hash", rolAsistente));
    }

    private CursoEvento guardarCurso(String nombre, String lugar, LocalDateTime inicio, int cupo) {
        CursoEvento curso = new CursoEvento();
        curso.setNombre(nombre);
        curso.setDuracionHoras(40);
        curso.setFechaInicio(inicio);
        curso.setFechaFin(inicio.plusDays(5));
        curso.setLugar(lugar);
        curso.setCupoMaximo(cupo);
        curso.setInstructor(instructor);
        return cursoRepositorio.save(curso);
    }

    private Inscripcion guardarInscripcion(Usuario usuario, CursoEvento curso, EstadoInscripcion estado) {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuario);
        inscripcion.setCursoEvento(curso);
        inscripcion.setEstado(estado);
        return inscripcionRepositorio.save(inscripcion);
    }

    @Test
    void debeBuscarCursosPorNombreSinDistinguirMayusculas() {
        guardarCurso("Trabajo en Alturas", "Pista A", LocalDateTime.now().plusDays(10), 20);
        guardarCurso("Espacios Confinados", "Aula 2", LocalDateTime.now().plusDays(20), 15);

        List<CursoEvento> resultado = cursoRepositorio.findAll(
                CursoEventoEspecificaciones.conFiltros("ALTURAS", null, null, null, null));

        assertEquals(1, resultado.size());
        assertEquals("Trabajo en Alturas", resultado.get(0).getNombre());
    }

    @Test
    void debeBuscarCursosPorRangoDeFechasYEstado() {
        guardarCurso("Curso cercano", "Pista A", LocalDateTime.now().plusDays(10), 20);
        guardarCurso("Curso lejano", "Pista B", LocalDateTime.now().plusDays(40), 20);

        List<CursoEvento> lejanos = cursoRepositorio.findAll(CursoEventoEspecificaciones.conFiltros(
                null, null, LocalDateTime.now().plusDays(30), null, EstadoCurso.PROGRAMADO));
        List<CursoEvento> cancelados = cursoRepositorio.findAll(CursoEventoEspecificaciones.conFiltros(
                null, null, null, null, EstadoCurso.CANCELADO));

        assertEquals(1, lejanos.size());
        assertEquals("Curso lejano", lejanos.get(0).getNombre());
        assertTrue(cancelados.isEmpty());
    }

    @Test
    void debeContarSoloInscripcionesQueOcupanCupo() {
        CursoEvento curso = guardarCurso("Trabajo en Alturas", "Pista A", LocalDateTime.now().plusDays(10), 20);
        guardarInscripcion(guardarAsistente("3000000001"), curso, EstadoInscripcion.PENDIENTE);
        guardarInscripcion(guardarAsistente("3000000002"), curso, EstadoInscripcion.PAGADO);
        guardarInscripcion(guardarAsistente("3000000003"), curso, EstadoInscripcion.CANCELADO);

        long ocupados = inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(
                curso.getId(), EstadoInscripcion.CANCELADO);

        assertEquals(2, ocupados);
    }

    @Test
    void noDebePermitirDosInscripcionesDelMismoUsuarioEnUnCurso() {
        CursoEvento curso = guardarCurso("Trabajo en Alturas", "Pista A", LocalDateTime.now().plusDays(10), 20);
        Usuario asistente = guardarAsistente("3000000001");
        guardarInscripcion(asistente, curso, EstadoInscripcion.PENDIENTE);
        inscripcionRepositorio.flush();

        assertThrows(DataIntegrityViolationException.class,
                () -> guardarInscripcion(asistente, curso, EstadoInscripcion.PENDIENTE));
    }

    @Test
    void debeBloquearYDevolverElCurso() {
        CursoEvento curso = guardarCurso("Trabajo en Alturas", "Pista A", LocalDateTime.now().plusDays(10), 20);

        assertTrue(cursoRepositorio.buscarPorIdConBloqueo(curso.getId()).isPresent());
    }

    @Test
    void alEliminarUnCursoSeEliminanSusInscripciones() {
        CursoEvento curso = guardarCurso("Trabajo en Alturas", "Pista A", LocalDateTime.now().plusDays(10), 20);
        guardarInscripcion(guardarAsistente("3000000001"), curso, EstadoInscripcion.PENDIENTE);
        Long idCurso = curso.getId();

        // Se limpia la memoria de JPA para cargar el curso como lo haria el servicio
        entityManager.flush();
        entityManager.clear();
        cursoRepositorio.delete(cursoRepositorio.findById(idCurso).orElseThrow());
        entityManager.flush();

        assertEquals(0, inscripcionRepositorio.count());
    }
}
