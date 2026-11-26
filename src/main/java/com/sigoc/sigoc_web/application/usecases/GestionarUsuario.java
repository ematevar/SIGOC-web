package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarUsuarioUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.time.Clock;
import java.util.List;

/**
 * CU-23: Administración de usuarios y catálogo de roles (exclusivo de ADMIN).
 */
public class GestionarUsuario implements GestionarUsuarioUseCase {

    private static final String PATRON_LOGIN = "[A-Za-z0-9._-]{3,40}";

    private final UsuarioPort usuarioPort;
    private final RolPort rolPort;
    private final PasswordHasherPort hasher;
    private final AuditoriaEventoPort auditoriaPort;
    private final Clock clock;

    public GestionarUsuario(UsuarioPort usuarioPort, RolPort rolPort, PasswordHasherPort hasher) {
        this(usuarioPort, rolPort, hasher, null, Clock.systemDefaultZone());
    }

    public GestionarUsuario(UsuarioPort usuarioPort, RolPort rolPort, PasswordHasherPort hasher,
                            AuditoriaEventoPort auditoriaPort, Clock clock) {
        this.usuarioPort = usuarioPort;
        this.rolPort = rolPort;
        this.hasher = hasher;
        this.auditoriaPort = auditoriaPort;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    @Override
    public List<Rol> listarRoles() {
        return rolPort.findAll();
    }

    @Override
    public List<Usuario> listar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_USUARIOS);
        return usuarioPort.findAll();
    }

    @Override
    public Usuario crear(UsuarioSesion actor, String nombre, String login, String clave, Integer idRol) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_USUARIOS);
        if (nombre == null || nombre.isBlank() || login == null || !login.trim().matches(PATRON_LOGIN)) {
            throw new ReglaNegocioException("Completa el nombre y un usuario válido (3 a 40 caracteres).");
        }
        if (clave == null || clave.length() < 8) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (idRol == null || idRol <= 0) {
            throw new ReglaNegocioException("Selecciona un rol válido.");
        }
        Rol rol = rolPort.findActivoById(idRol)
                .filter(r -> RolSistema.esValido(r.getNombreRol()))
                .orElseThrow(() -> new ReglaNegocioException("El rol seleccionado no es válido según el perfil del sistema."));
        String loginLimpio = login.trim();
        if (usuarioPort.existsByLogin(loginLimpio)) {
            throw new ReglaNegocioException("El nombre de usuario '" + loginLimpio + "' ya está registrado.");
        }
        Usuario nuevo = new Usuario(null, nombre.trim(), loginLimpio, rol.getNombreRol(),
                hasher.hash(clave), rol.getIdRol(), true, false);
        Usuario creado = usuarioPort.crear(nuevo);
        auditar(actor, "CREAR_USUARIO", String.valueOf(creado.getIdUsuario()),
                "Usuario " + loginLimpio + " con rol " + rol.getNombreRol());
        return creado;
    }

    @Override
    public void actualizar(UsuarioSesion actor, Integer idUsuario, String nombre, Integer idRol) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_USUARIOS);
        if (idUsuario == null || idUsuario <= 0 || nombre == null || nombre.isBlank() || idRol == null || idRol <= 0) {
            throw new ReglaNegocioException("Nombre o rol inválido.");
        }
        Rol nuevoRol = rolPort.findActivoById(idRol)
                .filter(r -> RolSistema.esValido(r.getNombreRol()))
                .orElseThrow(() -> new ReglaNegocioException("El rol seleccionado no es válido."));
        Usuario actual = usuarioPort.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        boolean eraAdmin = RolSistema.desde(actual.getRol()).orElse(null) == RolSistema.ADMIN;
        boolean seraAdmin = RolSistema.desde(nuevoRol.getNombreRol()).orElse(null) == RolSistema.ADMIN;
        if (actual.isActivo() && eraAdmin && !seraAdmin && usuarioPort.contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede cambiar el rol del único Administrador activo.");
        }
        usuarioPort.actualizarDatos(idUsuario, nombre.trim(), nuevoRol.getIdRol());
        auditar(actor, "ACTUALIZAR_USUARIO", String.valueOf(idUsuario),
                "Rol actualizado a " + nuevoRol.getNombreRol());
    }

    @Override
    public void cambiarEstado(UsuarioSesion actor, Integer idUsuario, boolean activo) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_USUARIOS);
        if (idUsuario == null || idUsuario <= 0) {
            throw new ReglaNegocioException("Usuario inválido.");
        }
        Usuario usuario = usuarioPort.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        if (!activo && usuario.isActivo()
                && RolSistema.desde(usuario.getRol()).orElse(null) == RolSistema.ADMIN
                && usuarioPort.contarAdminsActivos() <= 1) {
            throw new ReglaNegocioException("No se puede desactivar al único Administrador activo del sistema.");
        }
        usuarioPort.cambiarEstado(idUsuario, activo);
        auditar(actor, "CAMBIAR_ESTADO_USUARIO", String.valueOf(idUsuario),
                activo ? "Usuario activado" : "Usuario desactivado");
    }

    private void auditar(UsuarioSesion actor, String accion, String idEntidad, String detalle) {
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, accion, "usuario", idEntidad, "EXITO", detalle));
        }
    }
}
