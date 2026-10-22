package com.ecm.sigre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla certificado: certificado emitido a una inscripcion APROBADA. */
@Entity
@Table(name = "certificado")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_inscripcion", unique = true)
    private Inscripcion inscripcion;

    /** Codigo unico para verificar la autenticidad del certificado. */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    protected Certificado() {
    }

    public Certificado(Inscripcion inscripcion, String codigo) {
        this.inscripcion = inscripcion;
        this.codigo = codigo;
    }

    public Long getId() { return id; }
    public Inscripcion getInscripcion() { return inscripcion; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
}
