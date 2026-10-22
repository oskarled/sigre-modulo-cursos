package com.ecm.sigre.service;

import com.ecm.sigre.dto.InscripcionResponse;
import com.ecm.sigre.exception.RecursoNoEncontradoException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.CursoEvento;
import com.ecm.sigre.model.EstadoCurso;
import com.ecm.sigre.model.EstadoInscripcion;
import com.ecm.sigre.model.Inscripcion;
import com.ecm.sigre.model.NombreRol;
import com.ecm.sigre.model.Usuario;
import com.ecm.sigre.repository.InscripcionRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inscripciones de participantes en cursos. */
@Service
public class InscripcionService {

    /** Horas minimas antes del inicio del curso para poder cancelar. */
    static final long HORAS_MINIMAS_CANCELACION = 24;

    private final InscripcionRepository inscripcionRepository;
    private final CursoEventoService cursoService;
    private final UsuarioService usuarioService;

    public InscripcionService(InscripcionRepository inscripcionRepository, CursoEventoService cursoService,
                              UsuarioService usuarioService) {
        this.inscripcionRepository = inscripcionRepository;
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Inscribe un participante en un curso.
     * Reglas: el curso debe estar PROGRAMADO y no haber iniciado, el usuario debe ser ASISTENTE activo,
     * no puede estar ya inscrito y debe haber cupo.
     */
    @Transactional
    public InscripcionResponse inscribir(Long idCurso, Long idUsuario) {
        CursoEvento curso = cursoService.buscar(idCurso);
        Usuario usuario = usuarioService.buscar(idUsuario);
        if (usuario.getRol().getNombre() != NombreRol.ASISTENTE) {
            throw new ReglaNegocioException("Solo los usuarios con rol ASISTENTE pueden inscribirse en cursos");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario esta inactivo");
        }
        if (curso.getEstado() != EstadoCurso.PROGRAMADO || !curso.getFechaInicio().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("El curso no admite inscripciones: no esta programado o ya inicio");
        }
        if (inscripcionRepository.existsByCursoIdAndUsuarioIdAndEstadoNot(idCurso, idUsuario,
                EstadoInscripcion.CANCELADO)) {
            throw new ReglaNegocioException("El participante ya esta inscrito en este curso");
        }
        if (cursoService.inscritos(idCurso) >= curso.getCupoMaximo()) {
            throw new ReglaNegocioException("El curso no tiene cupos disponibles");
        }
        return InscripcionResponse.de(inscripcionRepository.save(new Inscripcion(usuario, curso)));
    }

    @Transactional(readOnly = true)
    public InscripcionResponse obtener(Long id) {
        return InscripcionResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> deCurso(Long idCurso) {
        cursoService.buscar(idCurso);
        return inscripcionRepository.findByCursoIdOrderByFechaInscripcion(idCurso).stream()
                .map(InscripcionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> deUsuario(Long idUsuario) {
        usuarioService.buscar(idUsuario);
        return inscripcionRepository.findByUsuarioIdOrderByFechaInscripcionDesc(idUsuario).stream()
                .map(InscripcionResponse::de).toList();
    }

    /**
     * Cancela una inscripcion y libera el cupo.
     * Regla: solo hasta 24 horas antes del inicio del curso.
     */
    @Transactional
    public InscripcionResponse cancelar(Long id) {
        Inscripcion inscripcion = buscar(id);
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADO) {
            throw new ReglaNegocioException("La inscripcion ya esta cancelada");
        }
        if (!inscripcion.getEstado().puedeCambiarA(EstadoInscripcion.CANCELADO)) {
            throw new ReglaNegocioException("No se puede cancelar una inscripcion " + inscripcion.getEstado());
        }
        long horas = Duration.between(LocalDateTime.now(), inscripcion.getCurso().getFechaInicio()).toHours();
        if (horas < HORAS_MINIMAS_CANCELACION) {
            throw new ReglaNegocioException("No se puede cancelar a menos de 24 horas del inicio del curso");
        }
        inscripcion.setEstado(EstadoInscripcion.CANCELADO);
        return InscripcionResponse.de(inscripcionRepository.save(inscripcion));
    }

    /**
     * Cambia el estado de la inscripcion: PENDIENTE -> PAGADO -> APROBADO | NO_APROBADO.
     * APROBADO y NO_APROBADO solo se registran cuando el curso ya inicio o finalizo.
     */
    @Transactional
    public InscripcionResponse cambiarEstado(Long id, String estado) {
        Inscripcion inscripcion = buscar(id);
        EstadoInscripcion nuevo = Enums.convertir(EstadoInscripcion.class, estado, "estado");
        if (nuevo == EstadoInscripcion.CANCELADO) {
            return cancelar(id);
        }
        if (!inscripcion.getEstado().puedeCambiarA(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar la inscripcion de " + inscripcion.getEstado()
                    + " a " + nuevo);
        }
        EstadoCurso estadoCurso = inscripcion.getCurso().getEstado();
        if ((nuevo == EstadoInscripcion.APROBADO || nuevo == EstadoInscripcion.NO_APROBADO)
                && estadoCurso != EstadoCurso.EN_CURSO && estadoCurso != EstadoCurso.FINALIZADO) {
            throw new ReglaNegocioException("Solo se califica (APROBADO o NO_APROBADO) cuando el curso esta EN_CURSO o FINALIZADO");
        }
        inscripcion.setEstado(nuevo);
        return InscripcionResponse.de(inscripcionRepository.save(inscripcion));
    }

    Inscripcion buscar(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la inscripcion con id " + id));
    }
}
