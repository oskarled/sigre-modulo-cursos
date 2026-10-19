package com.ecm.sigre.cursos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecm.sigre.common.exception.RecursoNoEncontradoException;
import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.InscripcionResponse;
import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.model.Inscripcion;
import com.ecm.sigre.cursos.repository.CursoEventoRepository;
import com.ecm.sigre.cursos.repository.InscripcionRepository;
import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.model.TipoDocumento;
import com.ecm.sigre.usuarios.model.Usuario;
import com.ecm.sigre.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Pruebas unitarias de las inscripciones y del control de aforo. Los repositorios se
 * simulan con Mockito, por lo que no se necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepositorio;

    @Mock
    private CursoEventoRepository cursoRepositorio;

    @Mock
    private UsuarioRepository usuarioRepositorio;

    @InjectMocks
    private InscripcionService servicio;

    /** Construye un curso con id 10 y el cupo y la fecha de inicio indicados. */
    private CursoEvento construirCurso(int cupo, LocalDateTime inicio) {
        CursoEvento curso = new CursoEvento();
        curso.setNombre("Trabajo en Alturas");
        curso.setDuracionHoras(40);
        curso.setFechaInicio(inicio);
        curso.setFechaFin(inicio.plusDays(5));
        curso.setLugar("Pista de entrenamiento");
        curso.setCupoMaximo(cupo);
        ReflectionTestUtils.setField(curso, "id", 10L);
        return curso;
    }

    /** Construye un usuario con id 3 y el rol indicado. */
    private Usuario construirUsuario(String nombreRol) {
        Usuario usuario = new Usuario(TipoDocumento.CC, "1000000003", "Maria", "Perez",
                "maria.perez@correo.com", "hash", new Rol(nombreRol));
        ReflectionTestUtils.setField(usuario, "id", 3L);
        return usuario;
    }

    /** Construye una inscripcion existente con id 7. */
    private Inscripcion construirInscripcion(Usuario usuario, CursoEvento curso, EstadoInscripcion estado) {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuario);
        inscripcion.setCursoEvento(curso);
        inscripcion.setEstado(estado);
        ReflectionTestUtils.setField(inscripcion, "id", 7L);
        return inscripcion;
    }

    @Test
    void inscribirDebeRegistrarCuandoHayCupoDisponible() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(10));
        Usuario usuario = construirUsuario(Rol.ASISTENTE);
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(usuario));
        when(inscripcionRepositorio.findByUsuarioIdAndCursoEventoId(3L, 10L)).thenReturn(Optional.empty());
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(2L);
        when(inscripcionRepositorio.save(any(Inscripcion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        InscripcionResponse respuesta = servicio.inscribir(10L, 3L);

        assertEquals(EstadoInscripcion.PENDIENTE, respuesta.estado());
        assertEquals(10L, respuesta.idCurso());
        assertEquals("Maria Perez", respuesta.nombreUsuario());
    }

    @Test
    void inscribirDebeFallarCuandoElCursoAlcanzoSuAforoMaximo() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(10));
        Usuario usuario = construirUsuario(Rol.ASISTENTE);
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(usuario));
        when(inscripcionRepositorio.findByUsuarioIdAndCursoEventoId(3L, 10L)).thenReturn(Optional.empty());
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(5L);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> servicio.inscribir(10L, 3L));

        assertEquals(HttpStatus.CONFLICT, error.getEstado());
        verify(inscripcionRepositorio, never()).save(any(Inscripcion.class));
    }

    @Test
    void inscribirDebeFallarSiElUsuarioYaEstaInscrito() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(10));
        Usuario usuario = construirUsuario(Rol.ASISTENTE);
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(usuario));
        when(inscripcionRepositorio.findByUsuarioIdAndCursoEventoId(3L, 10L))
                .thenReturn(Optional.of(construirInscripcion(usuario, curso, EstadoInscripcion.PENDIENTE)));

        assertThrows(ReglaNegocioException.class, () -> servicio.inscribir(10L, 3L));
        verify(inscripcionRepositorio, never()).save(any(Inscripcion.class));
    }

    @Test
    void inscribirDebeReactivarUnaInscripcionCancelada() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(10));
        Usuario usuario = construirUsuario(Rol.ASISTENTE);
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(usuario));
        when(inscripcionRepositorio.findByUsuarioIdAndCursoEventoId(3L, 10L))
                .thenReturn(Optional.of(construirInscripcion(usuario, curso, EstadoInscripcion.CANCELADO)));
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(1L);
        when(inscripcionRepositorio.save(any(Inscripcion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        InscripcionResponse respuesta = servicio.inscribir(10L, 3L);

        assertEquals(EstadoInscripcion.PENDIENTE, respuesta.estado());
        assertEquals(7L, respuesta.id());
    }

    @Test
    void inscribirDebeFallarSiElUsuarioNoTieneRolAsistente() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(10));
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(construirUsuario("ADMIN")));

        assertThrows(ReglaNegocioException.class, () -> servicio.inscribir(10L, 3L));
    }

    @Test
    void inscribirDebeFallarSiElCursoYaInicio() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().minusHours(1));
        when(cursoRepositorio.buscarPorIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(construirUsuario(Rol.ASISTENTE)));

        assertThrows(ReglaNegocioException.class, () -> servicio.inscribir(10L, 3L));
    }

    @Test
    void inscribirDebeFallarSiElCursoNoExiste() {
        when(cursoRepositorio.buscarPorIdConBloqueo(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.inscribir(99L, 3L));
    }

    @Test
    void cancelarDebeLiberarElCupo() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(3));
        Inscripcion inscripcion = construirInscripcion(construirUsuario(Rol.ASISTENTE), curso,
                EstadoInscripcion.PENDIENTE);
        when(inscripcionRepositorio.findById(7L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionRepositorio.save(inscripcion)).thenReturn(inscripcion);

        InscripcionResponse respuesta = servicio.cancelar(7L);

        assertEquals(EstadoInscripcion.CANCELADO, respuesta.estado());
    }

    @Test
    void cancelarDebeFallarSiFaltanMenosDe24HorasParaElInicio() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusHours(5));
        Inscripcion inscripcion = construirInscripcion(construirUsuario(Rol.ASISTENTE), curso,
                EstadoInscripcion.PENDIENTE);
        when(inscripcionRepositorio.findById(7L)).thenReturn(Optional.of(inscripcion));

        assertThrows(ReglaNegocioException.class, () -> servicio.cancelar(7L));
        verify(inscripcionRepositorio, never()).save(any(Inscripcion.class));
    }

    @Test
    void cancelarDebeFallarSiLaInscripcionYaEstaCancelada() {
        CursoEvento curso = construirCurso(5, LocalDateTime.now().plusDays(3));
        Inscripcion inscripcion = construirInscripcion(construirUsuario(Rol.ASISTENTE), curso,
                EstadoInscripcion.CANCELADO);
        when(inscripcionRepositorio.findById(7L)).thenReturn(Optional.of(inscripcion));

        assertThrows(ReglaNegocioException.class, () -> servicio.cancelar(7L));
    }

    @Test
    void listarPorUsuarioDebeFallarSiElUsuarioNoExiste() {
        when(usuarioRepositorio.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.listarPorUsuario(99L));
    }
}
