package com.ecm.sigre.model;

import jakarta.persistence.*;

/** Tabla instructor: datos profesionales de un usuario con rol INSTRUCTOR. */
@Entity
@Table(name = "instructor")
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instructor")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String especialidad;

    /** Numero de licencia en Seguridad y Salud en el Trabajo. */
    @Column(name = "licencia_sst", length = 40)
    private String licenciaSst;

    protected Instructor() {
    }

    public Instructor(Usuario usuario, String especialidad, String licenciaSst) {
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.licenciaSst = licenciaSst;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getLicenciaSst() { return licenciaSst; }
    public void setLicenciaSst(String licenciaSst) { this.licenciaSst = licenciaSst; }
}
