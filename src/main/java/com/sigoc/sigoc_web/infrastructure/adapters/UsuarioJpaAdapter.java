package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.infrastructure.entities.UsuarioEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.RolJpaRepository;
import com.sigoc.sigoc_web.infrastructure.repositories.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioJpaAdapter implements UsuarioPort {

    private final UsuarioJpaRepository repository;
    private final RolJpaRepository rolRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository repository, RolJpaRepository rolRepository) {
        this.repository = repository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return repository.findAllConRol().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Integer idUsuario) {
        return repository.findById(idUsuario).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findHabilitadoByLogin(String login) {
        return repository.findHabilitadoByLogin(login).map(this::toModel);
    }

    @Override
    public boolean existsByLogin(String login) {
        return repository.existsByUsuarioLoginIgnoreCase(login);
    }

    @Override
    public long contarAdminsActivos() {
        return repository.contarAdminsActivos();
    }

    @Override
    @Transactional
    public Usuario crear(Usuario usuario) {
        UsuarioEntity e = new UsuarioEntity();
        e.setNombreCompleto(usuario.getNombreCompleto());
        e.setUsuarioLogin(usuario.getUsername());
        e.setContrasenaHash(usuario.getPasswordHash());
        e.setRol(rolRepository.getReferenceById(usuario.getIdRol()));
        e.setEstado(1);
        e.setActivo(true);
        UsuarioEntity guardado = repository.saveAndFlush(e);
        return findById(guardado.getIdUsuario()).orElseThrow();
    }

    @Override
    @Transactional
    public void actualizarDatos(Integer idUsuario, String nombreCompleto, Integer idRol) {
        UsuarioEntity e = obtener(idUsuario);
        e.setNombreCompleto(nombreCompleto);
        e.setRol(rolRepository.getReferenceById(idRol));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idUsuario, boolean activo) {
        UsuarioEntity e = obtener(idUsuario);
        e.setActivo(activo);
        e.setEstado(activo ? 1 : 0);
        e.setFechaBorrado(activo ? null : LocalDateTime.now());
    }

    @Override
    @Transactional
    public void actualizarClave(Integer idUsuario, String passwordHash) {
        obtener(idUsuario).setContrasenaHash(passwordHash);
    }

    private UsuarioEntity obtener(Integer idUsuario) {
        return repository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private Usuario toModel(UsuarioEntity e) {
        return new Usuario(
                e.getIdUsuario(),
                e.getNombreCompleto(),
                e.getUsuarioLogin(),
                e.getRol().getNombreRol(),
                e.getContrasenaHash(),
                e.getRol().getIdRol(),
                e.isActivo(),
                false
        );
    }
}
