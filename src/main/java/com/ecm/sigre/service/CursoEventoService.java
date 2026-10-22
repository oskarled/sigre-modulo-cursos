package com.ecm.sigre.service;

import com.ecm.sigre.dto.CursoEventoRequest;
import com.ecm.sigre.dto.CursoEventoResponse;
import com.ecm.sigre.exception.DatosInvalidosException;
import com.ecm.sigre.exception.RecursoNoEncontradoException;
import com.ecm.sigre.exception.ReglaNegocioException;
import com.ecm.sigre.model.CursoEvento;
import com.ecm.sigre.model.EstadoCurso;
import com.ecm.sigre.model.EstadoInscripcion;
import com.ecm.sigre.repository.CursoEventoRepository;
import com.ecm.sigre.repository.InscripcionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de cursos y eventos, con sus reglas de fechas, cupos y estados. */
@Service
public class CursoEventoService {

    private final CursoEventoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final InstructorService instructorService;

    public CursoEventoService(CursoEventoRepository cursoRepository, InscripcionRepository inscripcionRepository,
                              InstructorService instructorService) {
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.instructorService = instructorService;
    }

    /**
     * Lista los cursos ordenados por fecha de inicio.
     *
     * @param estado filtro opcional (PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO)
     */
    @Transactional(readOnly = true)
    public List<CursoEventoResponse> listar(String estado) {
        List<CursoEvento> cursos = (estado == null || estado.isBlank())
                ? cursoRepository.findAllByOrderByFechaInicio()
                : cursoRepository.findByEstadoOrderByFechaInicio(Enums.convertir(EstadoCurso.class, estado, "estado"));
        return cursos.stream().map(this::respuesta).toList();
    }

    /** Cursos asignados a un instructor. */
    @Transactional(readOnly = true)
    public List<CursoEventoResponse> listarPorInstructor(Long idInstructor) {
        instructorService.buscar(idInstructor);
        return cursoRepository.findByInstructorIdOrderByFechaInicio(idInstructor).stream()
                .map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public CursoEventoResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    /**
     * Crea un curso en estado PROGRAMADO.
     * Reglas: la fecha de inicio debe ser futura y la de fin posterior a la de inicio.
     */
    @Transactional
    public CursoEventoResponse crear(CursoEventoRequest datos) {
        if (!datos.fechaInicio().isAfter(LocalDateTime.now())) {
            throw new DatosInvalidosException("La fecha de inicio debe ser posterior a la fecha actual");
        }
        validarFechas(datos);
        CursoEvento curso = new CursoEvento(datos.nombreCurso().trim(), datos.duracionHoras(), datos.fechaInicio(),
                datos.fechaFin(), datos.lugar().trim(), datos.cupoMaximo(),
                instructorService.buscar(datos.idInstructor()));
        return respuesta(cursoRepository.save(curso));
    }

    /**
     * Actualiza un curso. No se permite reducir el cupo por debajo de los inscritos
     * ni modificar cursos finalizados o cancelados.
     */
    @Transactional
    public CursoEventoResponse actualizar(Long id, CursoEventoRequest datos) {
        CursoEvento curso = buscar(id);
        if (curso.getEstado() == EstadoCurso.FINALIZADO || curso.getEstado() == EstadoCurso.CANCELADO) {
            throw new ReglaNegocioException("No se puede modificar un curso " + curso.getEstado());
        }
        validarFechas(datos);
        long inscritos = inscritos(id);
        if (datos.cupoMaximo() < inscritos) {
            throw new ReglaNegocioException("El cupo maximo no puede ser menor que los " + inscritos + " inscritos actuales");
        }
        curso.setNombreCurso(datos.nombreCurso().trim());
        curso.setDuracionHoras(datos.duracionHoras());
        curso.setFechaInicio(datos.fechaInicio());
        curso.setFechaFin(datos.fechaFin());
        curso.setLugar(datos.lugar().trim());
        curso.setCupoMaximo(datos.cupoMaximo());
        curso.setInstructor(instructorService.buscar(datos.idInstructor()));
        return respuesta(cursoRepository.save(curso));
    }

    /**
     * Cambia el estado del curso. Flujo: PROGRAMADO -> EN_CURSO -> FINALIZADO.
     * PROGRAMADO o EN_CURSO pueden pasar a CANCELADO.
     */
    @Transactional
    public CursoEventoResponse cambiarEstado(Long id, String estado) {
        CursoEvento curso = buscar(id);
        EstadoCurso nuevo = Enums.convertir(EstadoCurso.class, estado, "estado");
        boolean valido = switch (curso.getEstado()) {
            case PROGRAMADO -> nuevo == EstadoCurso.EN_CURSO || nuevo == EstadoCurso.CANCELADO;
            case EN_CURSO -> nuevo == EstadoCurso.FINALIZADO || nuevo == EstadoCurso.CANCELADO;
            case FINALIZADO, CANCELADO -> false;
        };
        if (!valido) {
            throw new ReglaNegocioException("No se puede pasar el curso de " + curso.getEstado() + " a " + nuevo);
        }
        curso.setEstado(nuevo);
        return respuesta(cursoRepository.save(curso));
    }

    /**
     * Elimina un curso sin inscripciones.
     *
     * @throws ReglaNegocioException si tiene inscripciones (se debe cancelar en su lugar)
     */
    @Transactional
    public void eliminar(Long id) {
        CursoEvento curso = buscar(id);
        if (inscripcionRepository.existsByCursoId(id)) {
            throw new ReglaNegocioException(
                    "El curso tiene inscripciones; cambie su estado a CANCELADO en lugar de eliminarlo");
        }
        cursoRepository.delete(curso);
    }

    CursoEvento buscar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el curso con id " + id));
    }

    long inscritos(Long idCurso) {
        return inscripcionRepository.countByCursoIdAndEstadoNot(idCurso, EstadoInscripcion.CANCELADO);
    }

    private CursoEventoResponse respuesta(CursoEvento curso) {
        return CursoEventoResponse.de(curso, inscritos(curso.getId()));
    }

    private void validarFechas(CursoEventoRequest datos) {
        if (!datos.fechaFin().isAfter(datos.fechaInicio())) {
            throw new DatosInvalidosException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}
