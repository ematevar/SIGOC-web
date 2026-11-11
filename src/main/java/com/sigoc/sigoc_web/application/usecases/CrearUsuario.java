package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-02: alta de usuarios con login único, contraseña robusta y rol válido.
 */
public class CrearUsuario {

    private static final String PATRON_LOGIN = "[A-Za-z0-9._-]{3,40}";

    private final UsuarioPort usuarioPort;
    private final RolPort rolPort;
    private final PasswordHasherPort hasher;

    public CrearUsuario(UsuarioPort usuarioPort, RolPort rolPort, PasswordHasherPort hasher) {
        this.usuarioPort = usuarioPort;
        this.rolPort = rolPort;
        this.hasher = hasher;
    }

    public Usuario ejecutar(UsuarioSesion actor, String nombre, String login, String clave, Integer idRol) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
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
        return usuarioPort.crear(nuevo);
    }
}
