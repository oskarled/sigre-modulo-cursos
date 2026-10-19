package com.ecm.sigre.cursos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecm.sigre.common.exception.RecursoNoEncontradoException;
import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.CursoEventoRequest;
import com.ecm.sigre.cursos.dto.CursoEventoResponse;
import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.repository.CursoEventoRepository;
import com.ecm.sigre.cursos.repository.InscripcionRepository;
import com.ecm.sigre.usuarios.model.Instructor;
import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.model.TipoDocumento;
import com.ecm.sigre.usuarios.model.Usuario;
import com.ecm.sigre.usuarios.repository.InstructorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pruebas unitarias de la logica de negocio de cursos. Los repositorios se simulan
 * con Mockito, por lo que no se necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class CursoEventoServiceTest {

    @Mock
    private CursoEventoRepository cursoRepositorio;

    @Mock
    private InscripcionRepository inscripcionRepositorio;

    @Mock
    private InstructorRepository instructorRepositorio;

    @InjectMocks
    private CursoEventoService servicio;

    /** Construye un instructor con id 1 y la licencia que vence en la fecha indicada. */
    private Instructor construirInstructor(LocalDate vencimientoLicencia) {
        Usuario usuario = new Usuario(TipoDocumento.CC, "1000000002", "Carlos", "Ortega",
                "instructor@ecm.com", "hash", new Rol("INSTRUCTOR"));
        Instructor instructor = new Instructor(usuario, "ALTURAS", "SST-1", vencimientoLicencia);
        ReflectionTestUtils.setField(instructor, "id", 1L);
        return instructor;
    }

    /** Construye una solicitud valida con las fechas y el cupo indicados. */
    private CursoEventoRequest construirSolicitud(LocalDateTime inicio, LocalDateTime fin, int cupo, Long idInstructor) {
        return new CursoEventoRequest("Trabajo en Alturas", 40, inicio, fin, "Pista de entrenamiento",
                cupo, idInstructor);
    }

    /** Construye un curso existente con id 10 que inicia en la fecha indicada. */
    private CursoEvento construirCurso(Instructor instructor, LocalDateTime inicio, int cupo) {
        CursoEvento curso = new CursoEvento();
        curso.setNombre("Trabajo en Alturas");
        curso.setDuracionHoras(40);
        curso.setFechaInicio(inicio);
        curso.setFechaFin(inicio.plusDays(5));
        curso.setLugar("Pista de entrenamiento");
        curso.setCupoMaximo(cupo);
        curso.setInstructor(instructor);
        ReflectionTestUtils.setField(curso, "id", 10L);
        return curso;
    }

    @Test
    void crearDebeGuardarUnCursoProgramadoConTodoElAforoLibre() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        when(instructorRepositorio.findById(1L)).thenReturn(Optional.of(instructor));
        when(cursoRepositorio.save(any(CursoEvento.class))).thenAnswer(invocacion -> {
            CursoEvento guardado = invocacion.getArgument(0);
            ReflectionTestUtils.setField(guardado, "id", 10L);
            return guardado;
        });
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(0L);

        CursoEventoResponse respuesta = servicio.crear(construirSolicitud(inicio, inicio.plusDays(5), 20, 1L));

        assertEquals(EstadoCurso.PROGRAMADO, respuesta.estado());
        assertEquals(20, respuesta.aforoDisponible());
        assertEquals("Carlos Ortega", respuesta.nombreInstructor());
    }

    @Test
    void crearDebeFallarSiLaFechaDeFinNoEsPosteriorALaDeInicio() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(construirSolicitud(inicio, inicio.minusHours(1), 20, 1L)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getEstado());
        verify(cursoRepositorio, never()).save(any(CursoEvento.class));
    }

    @Test
    void crearDebeFallarSiLaLicenciaDelInstructorEstaVencida() {
        Instructor instructor = construirInstructor(LocalDate.now().minusDays(1));
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        when(instructorRepositorio.findById(1L)).thenReturn(Optional.of(instructor));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(construirSolicitud(inicio, inicio.plusDays(5), 20, 1L)));

        assertEquals(HttpStatus.CONFLICT, error.getEstado());
        verify(cursoRepositorio, never()).save(any(CursoEvento.class));
    }

    @Test
    void crearDebeFallarSiElInstructorNoExiste() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        when(instructorRepositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.crear(construirSolicitud(inicio, inicio.plusDays(5), 20, 99L)));
    }

    @Test
    void actualizarDebeFallarSiElCupoQuedaPorDebajoDeLosInscritos() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        CursoEvento curso = construirCurso(instructor, inicio, 20);
        when(cursoRepositorio.findById(10L)).thenReturn(Optional.of(curso));
        when(instructorRepositorio.findById(1L)).thenReturn(Optional.of(instructor));
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(5L);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.actualizar(10L, construirSolicitud(inicio, inicio.plusDays(5), 3, 1L)));

        assertEquals(HttpStatus.CONFLICT, error.getEstado());
        verify(cursoRepositorio, never()).save(any(CursoEvento.class));
    }

    @Test
    void actualizarDebeFallarSiElCursoYaInicio() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        CursoEvento curso = construirCurso(instructor, LocalDateTime.now().minusHours(2), 20);
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        when(cursoRepositorio.findById(10L)).thenReturn(Optional.of(curso));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.actualizar(10L, construirSolicitud(inicio, inicio.plusDays(5), 20, 1L)));
    }

    @Test
    void eliminarDebeBorrarUnCursoQueNoHaIniciado() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        CursoEvento curso = construirCurso(instructor, LocalDateTime.now().plusDays(5), 20);
        when(cursoRepositorio.findById(10L)).thenReturn(Optional.of(curso));

        servicio.eliminar(10L);

        verify(cursoRepositorio).delete(curso);
    }

    @Test
    void eliminarDebeFallarSiElCursoYaInicio() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        CursoEvento curso = construirCurso(instructor, LocalDateTime.now().minusHours(1), 20);
        when(cursoRepositorio.findById(10L)).thenReturn(Optional.of(curso));

        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(10L));
        verify(cursoRepositorio, never()).delete(any(CursoEvento.class));
    }

    @Test
    void eliminarDebeFallarSiElCursoNoExiste() {
        when(cursoRepositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminar(99L));
    }

    @Test
    void cancelarDebeDeshabilitarElCurso() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        CursoEvento curso = construirCurso(instructor, LocalDateTime.now().plusDays(5), 20);
        when(cursoRepositorio.findById(10L)).thenReturn(Optional.of(curso));
        when(cursoRepositorio.save(curso)).thenReturn(curso);
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(0L);

        CursoEventoResponse respuesta = servicio.cancelar(10L);

        assertEquals(EstadoCurso.CANCELADO, respuesta.estado());
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscarDebeDevolverLosCursosConSuAforoDisponible() {
        Instructor instructor = construirInstructor(LocalDate.now().plusYears(1));
        CursoEvento curso = construirCurso(instructor, LocalDateTime.now().plusDays(5), 20);
        when(cursoRepositorio.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(curso));
        when(inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(10L, EstadoInscripcion.CANCELADO))
                .thenReturn(8L);

        List<CursoEventoResponse> resultado = servicio.buscar("alturas", null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals(12, resultado.get(0).aforoDisponible());
    }
}
