package com.ecm.sigre.usuarios.model;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Persona que interactua con el sistema (tabla {@code usuario}).
 *
 * <p>En este modulo se usa como participante de las inscripciones y como cuenta
 * del instructor. El registro y la autenticacion (HU-01, HU-02) pertenecen a otro modulo.</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    /** Identificador del usuario (PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    /** Tipo de documento de identidad. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    /** Numero de documento (unico). */
    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    /** Nombres del usuario. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Apellidos del usuario. */
    @Column(nullable = false, length = 100)
    private String apellido;

    /** Correo electronico (unico); se usa como nombre de usuario. */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String correo;

    /** Contrasena cifrada con BCrypt (nunca en texto plano). */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Rol de seguridad asignado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    /** Fecha y hora de registro en el sistema. */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Estado logico: {@code false} indica cuenta inhabilitada (no se borra fisicamente). */
    @Column(nullable = false)
    private boolean activo = true;

    /** Constructor sin argumentos requerido por JPA. */
    protected Usuario() {
    }

    /**
     * Crea un usuario con sus datos basicos.
     *
     * @param tipoDocumento   tipo de documento
     * @param numeroDocumento numero de documento
     * @param nombre          nombres
     * @param apellido        apellidos
     * @param correo          correo electronico
     * @param passwordHash    contrasena ya cifrada con BCrypt
     * @param rol             rol asignado
     */
    public Usuario(TipoDocumento tipoDocumento, String numeroDocumento, String nombre, String apellido,
                   String correo, String passwordHash, Rol rol) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    /** Asigna la fecha de registro antes de guardar por primera vez. */
    @PrePersist
    void asignarFechaRegistro() {
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Devuelve el nombre completo (nombres y apellidos).
     *
     * @return nombre completo del usuario
     */
    public String nombreCompleto() {
        return nombre + " " + apellido;
    }

    public Long getId() {
        return id;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
