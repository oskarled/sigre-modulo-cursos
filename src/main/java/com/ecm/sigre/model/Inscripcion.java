package com.ecm.sigre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla inscripcion: un asistente inscrito en un curso. */
@Entity
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso")
    private CursoEvento curso;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE;

    protected Inscripcion() {
    }

    public Inscripcion(Usuario usuario, CursoEvento curso) {
        this.usuario = usuario;
        this.curso = curso;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public CursoEvento getCurso() { return curso; }
    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public EstadoInscripcion getEstado() { return estado; }
    public void setEstado(EstadoInscripcion estado) { this.estado = estado; }
}
