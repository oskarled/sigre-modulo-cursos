package com.ecm.sigre.cursos.model;

import com.ecm.sigre.usuarios.model.Instructor;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sesion de curso programada en la academia (tabla {@code curso_evento}).
 *
 * <p>Define la programacion, el cupo maximo por seguridad industrial y el instructor
 * a cargo. Tiene una composicion con {@link Inscripcion}: al eliminar el curso se
 * eliminan sus inscripciones.</p>
 */
@Entity
@Table(name = "curso_evento")
public class CursoEvento {

    /** Identificador del curso (PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Long id;

    /** Nombre del curso (por ejemplo, Trabajo en Alturas). */
    @Column(name = "nombre_curso", nullable = false, length = 150)
    private String nombre;

    /** Intensidad horaria certificable. */
    @Column(name = "duracion_horas", nullable = false)
    private Integer duracionHoras;

    /** Fecha y hora de inicio. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /** Fecha y hora de finalizacion. */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /** Lugar donde se dicta el curso. */
    @Column(nullable = false, length = 150)
    private String lugar;

    /** Limite de aforo por motivos de seguridad. */
    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;

    /** Instructor a cargo del curso. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_instructor_cargo", nullable = false)
    private Instructor instructor;

    /** Estado actual del curso. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCurso estado = EstadoCurso.PROGRAMADO;

    /** Inscripciones del curso (composicion: se eliminan en cascada con el curso). */
    @OneToMany(mappedBy = "cursoEvento", cascade = CascadeType.REMOVE)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    /**
     * Calcula el aforo disponible (metodo del diagrama de clases).
     *
     * @param inscritosActivos numero de inscripciones que ocupan cupo
     * @return cupos libres, nunca negativo
     */
    public int obtenerAforoDisponible(long inscritosActivos) {
        return (int) Math.max(0, cupoMaximo - inscritosActivos);
    }

    /**
     * Indica si el curso ya inicio (su fecha de inicio es hoy o anterior).
     *
     * @return {@code true} si ya comenzo
     */
    public boolean haIniciado() {
        return !fechaInicio.isAfter(LocalDateTime.now());
    }

    /** Deshabilita el curso para impedir nuevas inscripciones (metodo del diagrama de clases). */
    public void deshabilitar() {
        this.estado = EstadoCurso.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDuracionHoras() {
        return duracionHoras;
    }

    public void setDuracionHoras(Integer duracionHoras) {
        this.duracionHoras = duracionHoras;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public EstadoCurso getEstado() {
        return estado;
    }

    public void setEstado(EstadoCurso estado) {
        this.estado = estado;
    }
}
