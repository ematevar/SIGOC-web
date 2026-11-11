package com.sigoc.sigoc_web.infraestructure.repositories;

import com.sigoc.sigoc_web.infraestructure.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer> {

    @Query("select u from UsuarioEntity u join fetch u.rol order by u.idUsuario")
    List<UsuarioEntity> findAllConRol();

    @Query("select u from UsuarioEntity u join fetch u.rol r "
            + "where u.usuarioLogin = :login and u.activo = true and u.estado = 1 and r.activo = true")
    Optional<UsuarioEntity> findHabilitadoByLogin(String login);

    boolean existsByUsuarioLoginIgnoreCase(String usuarioLogin);

    @Query("select count(u) from UsuarioEntity u where u.activo = true and upper(u.rol.nombreRol) like '%ADMIN%'")
    long contarAdminsActivos();
}
