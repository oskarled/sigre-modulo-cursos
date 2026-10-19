package com.ecm.sigre.cursos.model;

import com.ecm.sigre.usuarios.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Matricula de un participante en un curso (tabla {@code inscripcion}).
 *
 * <p>Un mismo usuario solo puede tener una inscripcion por curso (restriccion unica
 * compuesta). Al cancelarla, el cupo se libera.</p>
 */
@Entity
@Table(
        name = "inscripcion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inscripcion_usuario_curso",
                columnNames = {"id_usuario_participante", "id_curso"})
)
public class Inscripcion {

    /** Identificador de la inscripcion (PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Long id;

    /** Usuario participante (rol Asistente). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_participante", nullable = false)
    private Usuario usuario;

    /** Curso en el que se inscribe. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private CursoEvento cursoEvento;

    /** Fecha y hora de la inscripcion. */
    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    /** Estado de la matricula. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inscripcion", nullable = false, length = 20)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE;

    /**
     * Indica si la inscripcion ocupa cupo (es decir, no esta cancelada).
     *
     * @return {@code true} si la inscripcion esta activa
     */
    public boolean estaActiva() {
        return estado != EstadoInscripcion.CANCELADO;
    }

    /** Cancela la inscripcion y libera el cupo (metodo del diagrama de clases). */
    public void cancelar() {
        this.estado = EstadoInscripcion.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public CursoEvento getCursoEvento() {
        return cursoEvento;
    }

    public void setCursoEvento(CursoEvento cursoEvento) {
        this.cursoEvento = cursoEvento;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }
}
