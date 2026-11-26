package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.AutenticarUsuarioUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.CaptchaService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * CU-01: Generación de captcha e inicio de sesión con bloqueo temporal por intentos fallidos.
 */
public class AutenticarUsuario implements AutenticarUsuarioUseCase {

    public static final int MAX_INTENTOS_FALLIDOS = 5;
    public static final Duration BLOQUEO = Duration.ofSeconds(30);

    private final UsuarioPort usuarioPort;
    private final PasswordHasherPort hasher;
    private final IntentosLoginPort intentos;
    private final CaptchaService captcha;
    private final Clock clock;
    private final AuditoriaEventoPort auditoriaPort;

    public AutenticarUsuario(UsuarioPort usuarioPort, PasswordHasherPort hasher, IntentosLoginPort intentos,
                             CaptchaService captcha, Clock clock) {
        this(usuarioPort, hasher, intentos, captcha, clock, null);
    }

    public AutenticarUsuario(UsuarioPort usuarioPort, PasswordHasherPort hasher, IntentosLoginPort intentos,
                             CaptchaService captcha, Clock clock, AuditoriaEventoPort auditoriaPort) {
        this.usuarioPort = usuarioPort;
        this.hasher = hasher;
        this.intentos = intentos;
        this.captcha = captcha;
        this.clock = clock;
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    public String generarCaptcha() {
        return captcha.generar();
    }

    @Override
    public UsuarioSesion iniciarSesion(String usuario, String clave, String captchaEsperado, String captchaIngresado) {
        String clavePorLogin = usuario == null ? "" : usuario.trim().toLowerCase();
        Instant ahora = clock.instant();
        Optional<Instant> bloqueo = intentos.bloqueadoHasta(clavePorLogin);
        if (bloqueo.isPresent() && ahora.isBefore(bloqueo.get())) {
            long segs = Math.max(1L, (Duration.between(ahora, bloqueo.get()).toMillis() + 999) / 1000);
            auditar(null, clavePorLogin, "LOGIN_BLOQUEADO", "DENEGADO", "Cuenta bloqueada temporalmente");
            throw new ReglaNegocioException("Demasiados intentos fallidos. Espera " + segs
                    + " segundos antes de volver a intentar.");
        }
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            fallo(clavePorLogin, ahora);
            auditar(null, clavePorLogin, "LOGIN_FALLIDO", "FALLIDO", "Credenciales incompletas");
            throw new ReglaNegocioException("Ingresa usuario y contraseña.");
        }
        if (!captcha.validar(captchaEsperado, captchaIngresado)) {
            fallo(clavePorLogin, ahora);
            auditar(null, clavePorLogin, "LOGIN_FALLIDO", "FALLIDO", "Captcha inválido");
            throw new ReglaNegocioException("Captcha inválido.");
        }
        Usuario encontrado = usuarioPort.findHabilitadoByLogin(usuario.trim())
                .filter(u -> hasher.verificar(clave, u.getPasswordHash()))
                .orElse(null);
        Optional<RolSistema> rol = encontrado == null ? Optional.empty() : RolSistema.desde(encontrado.getRol());
        if (encontrado == null || rol.isEmpty()) {
            fallo(clavePorLogin, ahora);
            auditar(null, clavePorLogin, "LOGIN_FALLIDO", "FALLIDO", "Usuario o contraseña incorrectos");
            throw new ReglaNegocioException("Usuario o contraseña incorrectos, o cuenta inactiva.");
        }
        intentos.reiniciar(clavePorLogin);
        UsuarioSesion sesion = new UsuarioSesion(
                encontrado.getIdUsuario(),
                encontrado.getUsername(),
                encontrado.getNombreCompleto(),
                rol.get(),
                hasher.esClaveInicial(encontrado.getPasswordHash())
        );
        auditar(sesion, sesion.username(), "LOGIN_OK", "EXITO", "Inicio de sesión exitoso");
        return sesion;
    }

    private void fallo(String clavePorLogin, Instant ahora) {
        if (intentos.registrarFallo(clavePorLogin) >= MAX_INTENTOS_FALLIDOS) {
            intentos.bloquear(clavePorLogin, ahora.plus(BLOQUEO));
        }
    }

    private void auditar(UsuarioSesion actor, String login, String accion, String resultado, String detalle) {
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, accion, "usuario", login, resultado, detalle));
        }
    }
}
