package com.ecm.sigre.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Instructor acreditado ante el Ministerio del Trabajo (tabla {@code instructor}).
 *
 * <p>Se asocia 1:1 con un {@link Usuario} y guarda los datos de su licencia de
 * Seguridad y Salud en el Trabajo (SST).</p>
 */
@Entity
@Table(name = "instructor")
public class Instructor {

    /** Identificador del instructor (PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instructor")
    private Long id;

    /** Cuenta de acceso del instructor (relacion 1:1). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    /** Especialidad tecnica acreditada (ALTURAS, ESPACIOS CONFINADOS, BRIGADAS). */
    @Column(nullable = false, length = 100)
    private String especialidad;

    /** Numero de licencia SST (unico). */
    @Column(name = "numero_licencia_alturas", nullable = false, unique = true, length = 50)
    private String numeroLicencia;

    /** Fecha limite de vigencia de la licencia. */
    @Column(name = "fecha_vencimiento_licencia", nullable = false)
    private LocalDate fechaVencimientoLicencia;

    /** Constructor sin argumentos requerido por JPA. */
    protected Instructor() {
    }

    /**
     * Crea un instructor con sus datos de acreditacion.
     *
     * @param usuario                  cuenta de acceso del instructor
     * @param especialidad             especialidad acreditada
     * @param numeroLicencia           numero de licencia SST
     * @param fechaVencimientoLicencia fecha de vencimiento de la licencia
     */
    public Instructor(Usuario usuario, String especialidad, String numeroLicencia,
                      LocalDate fechaVencimientoLicencia) {
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.numeroLicencia = numeroLicencia;
        this.fechaVencimientoLicencia = fechaVencimientoLicencia;
    }

    /**
     * Indica si la licencia SST sigue vigente a la fecha actual (metodo del diagrama de clases).
     *
     * @return {@code true} si la licencia no ha vencido
     */
    public boolean validarLicencia() {
        return !fechaVencimientoLicencia.isBefore(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public LocalDate getFechaVencimientoLicencia() {
        return fechaVencimientoLicencia;
    }
}
