package com.ecm.sigre.model;

import jakarta.persistence.*;

/** Tabla rol: ASISTENTE, INSTRUCTOR o ADMIN. */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NombreRol nombre;

    protected Rol() {
    }

    public Rol(NombreRol nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public NombreRol getNombre() {
        return nombre;
    }
}
