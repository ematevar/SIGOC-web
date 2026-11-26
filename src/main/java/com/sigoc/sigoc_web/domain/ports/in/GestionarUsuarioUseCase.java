package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.List;

/**
 * CU-23: Administración de usuarios y catálogo de roles (exclusivo de ADMIN).
 */
public interface GestionarUsuarioUseCase {
    List<Rol> listarRoles();

    List<Usuario> listar(UsuarioSesion actor);

    Usuario crear(UsuarioSesion actor, String nombreCompleto, String username, String clave, Integer idRol);

    void actualizar(UsuarioSesion actor, Integer idUsuario, String nombreCompleto, Integer idRol);

    void cambiarEstado(UsuarioSesion actor, Integer idUsuario, boolean activo);
}
