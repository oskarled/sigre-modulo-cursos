package com.ecm.sigre.cursos.service;

import com.ecm.sigre.common.exception.RecursoNoEncontradoException;
import com.ecm.sigre.common.exception.ReglaNegocioException;
import com.ecm.sigre.cursos.dto.InscripcionResponse;
import com.ecm.sigre.cursos.model.CursoEvento;
import com.ecm.sigre.cursos.model.EstadoCurso;
import com.ecm.sigre.cursos.model.EstadoInscripcion;
import com.ecm.sigre.cursos.model.Inscripcion;
import com.ecm.sigre.cursos.repository.CursoEventoRepository;
import com.ecm.sigre.cursos.repository.InscripcionRepository;
import com.ecm.sigre.usuarios.model.Rol;
import com.ecm.sigre.usuarios.model.Usuario;
import com.ecm.sigre.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de las inscripciones (HU-09, HU-10 y HU-11) con control de aforo.
 *
 * <p>Para evitar la sobreventa de cupos cuando varios usuarios se inscriben al mismo
 * tiempo, la inscripcion bloquea la fila del curso (bloqueo pesimista) dentro de una
 * transaccion, tal como define la arquitectura del SIGRE.</p>
 */
@Service
public class InscripcionService {

    /** Horas minimas de anticipacion para poder cancelar una inscripcion. */
    private static final long HORAS_LIMITE_CANCELACION = 24;

    private static final String MENSAJE_CURSO_NO_ENCONTRADO = "No existe un curso con id %d";
    private static final String MENSAJE_USUARIO_NO_ENCONTRADO = "No existe un usuario con id %d";
    private static final String MENSAJE_INSCRIPCION_NO_ENCONTRADA = "No existe una inscripcion con id %d";

    private final InscripcionRepository inscripcionRepositorio;
    private final CursoEventoRepository cursoRepositorio;
    private final UsuarioRepository usuarioRepositorio;

    /**
     * Crea el servicio con sus repositorios (inyeccion de dependencias por constructor).
     *
     * @param inscripcionRepositorio repositorio de inscripciones
     * @param cursoRepositorio       repositorio de cursos
     * @param usuarioRepositorio     repositorio de usuarios
     */
    public InscripcionService(InscripcionRepository inscripcionRepositorio,
                              CursoEventoRepository cursoRepositorio,
                              UsuarioRepository usuarioRepositorio) {
        this.inscripcionRepositorio = inscripcionRepositorio;
        this.cursoRepositorio = cursoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Inscribe a un usuario en un curso verificando el aforo (HU-09).
     *
     * <p>Si el usuario tenia una inscripcion cancelada en el mismo curso, se reactiva.</p>
     *
     * @param idCurso   identificador del curso
     * @param idUsuario identificador del participante
     * @return la inscripcion registrada
     * @throws RecursoNoEncontradoException si el curso o el usuario no existen
     * @throws ReglaNegocioException        si el curso no admite inscripciones, el usuario no puede inscribirse,
     *                                      ya esta inscrito o no hay cupos
     */
    @Transactional
    public InscripcionResponse inscribir(Long idCurso, Long idUsuario) {
        // El bloqueo de la fila del curso serializa las inscripciones concurrentes
        CursoEvento curso = cursoRepositorio.buscarPorIdConBloqueo(idCurso)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        String.format(MENSAJE_CURSO_NO_ENCONTRADO, idCurso)));
        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        String.format(MENSAJE_USUARIO_NO_ENCONTRADO, idUsuario)));

        validarUsuarioPuedeInscribirse(usuario);
        if (curso.getEstado() != EstadoCurso.PROGRAMADO || curso.haIniciado()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El curso no esta abierto a inscripciones");
        }

        Inscripcion inscripcion = inscripcionRepositorio.findByUsuarioIdAndCursoEventoId(idUsuario, idCurso)
                .orElseGet(Inscripcion::new);
        if (inscripcion.getId() != null && inscripcion.estaActiva()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El usuario ya esta inscrito en este curso");
        }

        long inscritos = inscripcionRepositorio
                .countByCursoEventoIdAndEstadoNot(idCurso, EstadoInscripcion.CANCELADO);
        if (inscritos >= curso.getCupoMaximo()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No hay cupos disponibles: el curso alcanzo su aforo maximo");
        }

        inscripcion.setUsuario(usuario);
        inscripcion.setCursoEvento(curso);
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE);
        inscripcion.setFechaInscripcion(LocalDateTime.now());
        return InscripcionResponse.desde(inscripcionRepositorio.save(inscripcion));
    }

    /**
     * Cancela una inscripcion y libera el cupo (HU-10).
     *
     * @param idInscripcion identificador de la inscripcion
     * @return la inscripcion cancelada
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si ya estaba cancelada o faltan menos de 24 horas para el inicio
     */
    @Transactional
    public InscripcionResponse cancelar(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        String.format(MENSAJE_INSCRIPCION_NO_ENCONTRADA, idInscripcion)));

        if (!inscripcion.estaActiva()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "La inscripcion ya esta cancelada");
        }
        LocalDateTime limite = inscripcion.getCursoEvento().getFechaInicio().minusHours(HORAS_LIMITE_CANCELACION);
        if (LocalDateTime.now().isAfter(limite)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Solo se puede cancelar hasta 24 horas antes del inicio del curso");
        }

        inscripcion.cancelar();
        return InscripcionResponse.desde(inscripcionRepositorio.save(inscripcion));
    }

    /**
     * Lista las inscripciones de un usuario (HU-11).
     *
     * @param idUsuario identificador del usuario
     * @return inscripciones del usuario, de la mas reciente a la mas antigua
     * @throws RecursoNoEncontradoException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarPorUsuario(Long idUsuario) {
        if (!usuarioRepositorio.existsById(idUsuario)) {
            throw new RecursoNoEncontradoException(String.format(MENSAJE_USUARIO_NO_ENCONTRADO, idUsuario));
        }
        return inscripcionRepositorio.findByUsuarioIdOrderByFechaInscripcionDesc(idUsuario)
                .stream()
                .map(InscripcionResponse::desde)
                .toList();
    }

    /**
     * Lista los asistentes inscritos en un curso (RF-05).
     *
     * @param idCurso identificador del curso
     * @return inscripciones del curso, de la mas antigua a la mas reciente
     * @throws RecursoNoEncontradoException si el curso no existe
     */
    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarPorCurso(Long idCurso) {
        if (!cursoRepositorio.existsById(idCurso)) {
            throw new RecursoNoEncontradoException(String.format(MENSAJE_CURSO_NO_ENCONTRADO, idCurso));
        }
        return inscripcionRepositorio.findByCursoEventoIdOrderByFechaInscripcionAsc(idCurso)
                .stream()
                .map(InscripcionResponse::desde)
                .toList();
    }

    // ---------------------------------------------------------------------
    // Metodos privados de apoyo
    // ---------------------------------------------------------------------

    /** Solo los usuarios activos con rol ASISTENTE pueden inscribirse a un curso. */
    private void validarUsuarioPuedeInscribirse(Usuario usuario) {
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "La cuenta del usuario esta inhabilitada");
        }
        if (!Rol.ASISTENTE.equals(usuario.getRol().getNombre())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Solo los usuarios con rol ASISTENTE pueden inscribirse a un curso");
        }
    }
}
