package com.ecm.sigre.cursos.service;

import com.ecm.sigre.common.exception.RecursoNoEncontradoException;
import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.CursoEventoRequest;
import com.ecm.sigre.cursos.dto.CursoEventoResponse;
import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.repository.CursoEventoRepository;
import com.ecm.sigre.cursos.repository.InscripcionRepository;
import com.ecm.sigre.cursos.specification.CursoEventoEspecificaciones;
import com.ecm.sigre.usuarios.model.Instructor;
import com.ecm.sigre.usuarios.repository.InstructorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de los cursos/eventos (HU-05 a HU-08).
 *
 * <p>Implementa el CRUD de cursos y sus reglas: fechas coherentes, instructor con
 * licencia SST vigente, cupo no menor a los inscritos y eliminacion solo de cursos
 * que aun no han iniciado.</p>
 */
@Service
public class CursoEventoService {

    private static final String MENSAJE_CURSO_NO_ENCONTRADO = "No existe un curso con id %d";
    private static final String MENSAJE_INSTRUCTOR_NO_ENCONTRADO = "No existe un instructor con id %d";

    private final CursoEventoRepository cursoRepositorio;
    private final InscripcionRepository inscripcionRepositorio;
    private final InstructorRepository instructorRepositorio;

    /**
     * Crea el servicio con sus repositorios (inyeccion de dependencias por constructor).
     *
     * @param cursoRepositorio       repositorio de cursos
     * @param inscripcionRepositorio repositorio de inscripciones
     * @param instructorRepositorio  repositorio de instructores
     */
    public CursoEventoService(CursoEventoRepository cursoRepositorio,
                              InscripcionRepository inscripcionRepositorio,
                              InstructorRepository instructorRepositorio) {
        this.cursoRepositorio = cursoRepositorio;
        this.inscripcionRepositorio = inscripcionRepositorio;
        this.instructorRepositorio = instructorRepositorio;
    }

    /**
     * Crea un curso (insercion, HU-05).
     *
     * @param solicitud datos del curso
     * @return el curso creado
     * @throws ReglaNegocioException        si las fechas son incoherentes o la licencia del instructor esta vencida
     * @throws RecursoNoEncontradoException si el instructor no existe
     */
    @Transactional
    public CursoEventoResponse crear(CursoEventoRequest solicitud) {
        validarFechas(solicitud);
        Instructor instructor = buscarInstructorConLicenciaVigente(solicitud.idInstructor());

        CursoEvento curso = new CursoEvento();
        copiarDatos(solicitud, curso, instructor);
        curso.setEstado(EstadoCurso.PROGRAMADO);

        return construirRespuesta(cursoRepositorio.save(curso));
    }

    /**
     * Busca cursos con filtros opcionales (consulta, HU-08).
     *
     * @param nombre texto contenido en el nombre (opcional)
     * @param lugar  texto contenido en el lugar (opcional)
     * @param desde  primer dia del rango de fecha de inicio, incluido (opcional)
     * @param hasta  ultimo dia del rango de fecha de inicio, incluido (opcional)
     * @param estado estado del curso (opcional)
     * @return cursos que cumplen los filtros, ordenados por fecha de inicio
     */
    @Transactional(readOnly = true)
    public List<CursoEventoResponse> buscar(String nombre, String lugar, LocalDate desde,
                                            LocalDate hasta, EstadoCurso estado) {
        LocalDateTime desdeInclusive = desde == null ? null : desde.atStartOfDay();
        // El dia final se incluye completo: se consulta hasta el inicio del dia siguiente
        LocalDateTime hastaExclusivo = hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        return cursoRepositorio
                .findAll(CursoEventoEspecificaciones.conFiltros(nombre, lugar, desdeInclusive, hastaExclusivo, estado),
                        Sort.by(Sort.Direction.ASC, "fechaInicio"))
                .stream()
                .map(this::construirRespuesta)
                .toList();
    }

    /**
     * Consulta el detalle de un curso (HU-08).
     *
     * @param id identificador del curso
     * @return el curso encontrado
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public CursoEventoResponse obtenerPorId(Long id) {
        return construirRespuesta(buscarPorId(id));
    }

    /**
     * Actualiza un curso (actualizacion, HU-06).
     *
     * @param id        identificador del curso
     * @param solicitud nuevos datos
     * @return el curso actualizado
     * @throws RecursoNoEncontradoException si el curso o el instructor no existen
     * @throws ReglaNegocioException        si el curso ya inicio o fue cancelado, si las fechas son
     *                                      incoherentes o si el cupo queda por debajo de los inscritos
     */
    @Transactional
    public CursoEventoResponse actualizar(Long id, CursoEventoRequest solicitud) {
        CursoEvento curso = buscarPorId(id);

        if (curso.getEstado() != EstadoCurso.PROGRAMADO || curso.haIniciado()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Solo se pueden editar cursos programados que aun no han iniciado");
        }
        validarFechas(solicitud);
        Instructor instructor = buscarInstructorConLicenciaVigente(solicitud.idInstructor());

        long inscritos = contarInscritosActivos(id);
        if (solicitud.cupoMaximo() < inscritos) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El cupo maximo (" + solicitud.cupoMaximo()
                            + ") no puede ser menor a los inscritos actuales (" + inscritos + ")");
        }

        copiarDatos(solicitud, curso, instructor);
        return construirRespuesta(cursoRepositorio.save(curso));
    }

    /**
     * Cancela (deshabilita) un curso para impedir nuevas inscripciones.
     *
     * @param id identificador del curso
     * @return el curso con estado CANCELADO
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional
    public CursoEventoResponse cancelar(Long id) {
        CursoEvento curso = buscarPorId(id);
        curso.deshabilitar();
        return construirRespuesta(cursoRepositorio.save(curso));
    }

    /**
     * Elimina un curso y, en cascada, sus inscripciones (eliminacion, HU-07).
     *
     * @param id identificador del curso
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si el curso ya inicio
     */
    @Transactional
    public void eliminar(Long id) {
        CursoEvento curso = buscarPorId(id);
        if (curso.haIniciado()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Solo se pueden eliminar cursos que aun no han iniciado");
        }
        cursoRepositorio.delete(curso);
    }

    // ---------------------------------------------------------------------
    // Metodos privados de apoyo
    // ---------------------------------------------------------------------

    /** Busca un curso por id o lanza la excepcion correspondiente. */
    private CursoEvento buscarPorId(Long id) {
        return cursoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(String.format(MENSAJE_CURSO_NO_ENCONTRADO, id)));
    }

    /** Busca el instructor y verifica que su licencia SST este vigente. */
    private Instructor buscarInstructorConLicenciaVigente(Long idInstructor) {
        Instructor instructor = instructorRepositorio.findById(idInstructor)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        String.format(MENSAJE_INSTRUCTOR_NO_ENCONTRADO, idInstructor)));
        if (!instructor.validarLicencia()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El instructor tiene la licencia SST vencida y no puede ser asignado a un curso");
        }
        return instructor;
    }

    /** Verifica que la fecha de fin sea posterior a la de inicio. */
    private void validarFechas(CursoEventoRequest solicitud) {
        if (!solicitud.fechaFin().isAfter(solicitud.fechaInicio())) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    /** Copia los datos de la solicitud en la entidad, limpiando espacios sobrantes. */
    private void copiarDatos(CursoEventoRequest solicitud, CursoEvento curso, Instructor instructor) {
        curso.setNombre(solicitud.nombreCurso().trim());
        curso.setDuracionHoras(solicitud.duracionHoras());
        curso.setFechaInicio(solicitud.fechaInicio());
        curso.setFechaFin(solicitud.fechaFin());
        curso.setLugar(solicitud.lugar().trim());
        curso.setCupoMaximo(solicitud.cupoMaximo());
        curso.setInstructor(instructor);
    }

    /** Cuenta las inscripciones que ocupan cupo (todas menos las canceladas). */
    private long contarInscritosActivos(Long idCurso) {
        return inscripcionRepositorio.countByCursoEventoIdAndEstadoNot(idCurso, EstadoInscripcion.CANCELADO);
    }

    /** Construye la respuesta del curso con su aforo disponible. */
    private CursoEventoResponse construirRespuesta(CursoEvento curso) {
        return CursoEventoResponse.desde(curso, contarInscritosActivos(curso.getId()));
    }
}
