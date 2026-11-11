package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;

import java.time.Instant;
import java.util.*;

/**
 * Implementaciones en memoria de los puertos de salida para pruebas unitarias de casos de uso.
 */
final class Fakes {

    private Fakes() {
    }

    static final class RolesEnMemoria implements RolPort {
        final Map<Integer, Rol> roles = new LinkedHashMap<>();
        final Set<Integer> inactivos = new HashSet<>();

        RolesEnMemoria() {
            roles.put(1, new Rol(1, "ADMIN"));
            roles.put(2, new Rol(2, "RESIDENTE"));
            roles.put(3, new Rol(3, "ALMACENERO"));
            roles.put(4, new Rol(4, "CONTADOR"));
        }

        @Override
        public List<Rol> findAll() {
            return new ArrayList<>(roles.values());
        }

        @Override
        public Optional<Rol> findById(Integer idRol) {
            return Optional.ofNullable(roles.get(idRol));
        }

        @Override
        public List<Rol> findAllActivos() {
            return roles.values().stream().filter(r -> !inactivos.contains(r.getIdRol())).toList();
        }

        @Override
        public Optional<Rol> findActivoById(Integer idRol) {
            return findById(idRol).filter(r -> !inactivos.contains(r.getIdRol()));
        }
    }

    static final class UsuariosEnMemoria implements UsuarioPort {
        final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
        private int secuencia = 1;

        Usuario agregar(String login, String rol, int idRol, String hash, boolean activo) {
            Usuario u = new Usuario(secuencia++, "Nombre " + login, login, rol, hash, idRol, activo, false);
            usuarios.put(u.getIdUsuario(), u);
            return u;
        }

        @Override
        public List<Usuario> findAll() {
            return new ArrayList<>(usuarios.values());
        }

        @Override
        public Optional<Usuario> findById(Integer idUsuario) {
            return Optional.ofNullable(usuarios.get(idUsuario));
        }

        @Override
        public Optional<Usuario> findHabilitadoByLogin(String login) {
            return usuarios.values().stream().filter(u -> u.getUsername().equals(login) && u.isActivo()).findFirst();
        }

        @Override
        public boolean existsByLogin(String login) {
            return usuarios.values().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(login));
        }

        @Override
        public long contarAdminsActivos() {
            return usuarios.values().stream().filter(u -> u.isActivo() && u.getRol().contains("ADMIN")).count();
        }

        @Override
        public Usuario crear(Usuario usuario) {
            usuario.setIdUsuario(secuencia++);
            usuarios.put(usuario.getIdUsuario(), usuario);
            return usuario;
        }

        @Override
        public void actualizarDatos(Integer idUsuario, String nombreCompleto, Integer idRol) {
            Usuario u = usuarios.get(idUsuario);
            u.setNombreCompleto(nombreCompleto);
            u.setIdRol(idRol);
        }

        @Override
        public void cambiarEstado(Integer idUsuario, boolean activo) {
            usuarios.get(idUsuario).setActivo(activo);
        }

        @Override
        public void actualizarClave(Integer idUsuario, String passwordHash) {
            usuarios.get(idUsuario).setPasswordHash(passwordHash);
        }
    }

    /** Hasher trivial: "H:" + clave. La clave inicial se marca con "H:INICIAL:". */
    static final class HasherFalso implements PasswordHasherPort {
        @Override
        public String hash(String clave) {
            return "H:" + clave;
        }

        @Override
        public boolean verificar(String clave, String hashAlmacenado) {
            return hashAlmacenado != null && (hashAlmacenado.equals("H:" + clave)
                    || hashAlmacenado.equals("H:INICIAL:" + clave));
        }

        @Override
        public boolean esClaveInicial(String hashAlmacenado) {
            return hashAlmacenado != null && hashAlmacenado.startsWith("H:INICIAL:");
        }
    }

    static final class IntentosEnMemoria implements IntentosLoginPort {
        final Map<String, Integer> fallos = new HashMap<>();
        final Map<String, Instant> bloqueos = new HashMap<>();

        @Override
        public Optional<Instant> bloqueadoHasta(String clave) {
            return Optional.ofNullable(bloqueos.get(clave));
        }

        @Override
        public int registrarFallo(String clave) {
            return fallos.merge(clave, 1, Integer::sum);
        }

        @Override
        public void bloquear(String clave, Instant hasta) {
            bloqueos.put(clave, hasta);
            fallos.remove(clave);
        }

        @Override
        public void reiniciar(String clave) {
            fallos.remove(clave);
            bloqueos.remove(clave);
        }
    }
}
