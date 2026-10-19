package com.ecm.sigre.cursos.repository;

import com.ecm.sigre.cursos.model.CursoEvento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de cursos. Hereda el CRUD de {@link JpaRepository} y la busqueda con
 * filtros dinamicos de {@link JpaSpecificationExecutor}.
 */
@Repository
public interface CursoEventoRepository
        extends JpaRepository<CursoEvento, Long>, JpaSpecificationExecutor<CursoEvento> {

    /**
     * Busca un curso bloqueando su fila para escritura (bloqueo pesimista, equivalente a
     * {@code SELECT ... FOR UPDATE}). Se usa al inscribir para que las solicitudes
     * concurrentes sobre el mismo curso se procesen una a una y no se supere el aforo.
     *
     * @param id identificador del curso
     * @return el curso con la fila bloqueada, o vacio si no existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CursoEvento c where c.id = :id")
    Optional<CursoEvento> buscarPorIdConBloqueo(@Param("id") Long id);
}
