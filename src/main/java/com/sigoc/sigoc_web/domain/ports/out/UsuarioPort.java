package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioPort {
    List<Usuario> findAll();

    Optional<Usuario> findById(Integer idUsuario);

    /** Usuario con activo=1, estado=1 y rol activo (condición para iniciar sesión). */
    Optional<Usuario> findHabilitadoByLogin(String login);

    boolean existsByLogin(String login);

    long contarAdminsActivos();

    Usuario crear(Usuario usuario);

    void actualizarDatos(Integer idUsuario, String nombreCompleto, Integer idRol);

    void cambiarEstado(Integer idUsuario, boolean activo);

    void actualizarClave(Integer idUsuario, String passwordHash);
}
