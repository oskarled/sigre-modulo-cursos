package com.ecm.sigre.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Rol de seguridad del sistema (tabla {@code rol}).
 *
 * <p>Valores del dominio: ADMIN, COORDINADOR, INSTRUCTOR y ASISTENTE.</p>
 */
@Entity
@Table(name = "rol")
public class Rol {

    /** Nombre del rol de los participantes que se inscriben a los cursos. */
    public static final String ASISTENTE = "ASISTENTE";

    /** Identificador del rol (PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    /** Nombre unico del rol. */
    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombre;

    /** Constructor sin argumentos requerido por JPA. */
    protected Rol() {
    }

    /**
     * Crea un rol con el nombre indicado.
     *
     * @param nombre nombre del rol
     */
    public Rol(String nombre) {
        this.nombre = nombre;
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
}
