package com.ecm.sigre.repository;

import com.ecm.sigre.model.Certificado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla certificado. */
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    Optional<Certificado> findByCodigo(String codigo);

    boolean existsByInscripcionId(Long idInscripcion);

    List<Certificado> findByInscripcionUsuarioIdOrderByFechaEmisionDesc(Long idUsuario);

    List<Certificado> findAllByOrderByFechaEmisionDesc();
}
