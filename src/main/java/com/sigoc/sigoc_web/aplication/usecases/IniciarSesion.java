package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.CaptchaService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * RF-01: inicio de sesión con captcha y bloqueo temporal tras intentos fallidos.
 */
public class IniciarSesion {

    public static final int MAX_INTENTOS_FALLIDOS = 5;
    public static final Duration BLOQUEO = Duration.ofSeconds(30);

    private final UsuarioPort usuarioPort;
    private final PasswordHasherPort hasher;
    private final IntentosLoginPort intentos;
    private final CaptchaService captcha;
    private final Clock clock;

    public IniciarSesion(UsuarioPort usuarioPort, PasswordHasherPort hasher, IntentosLoginPort intentos,
                         CaptchaService captcha, Clock clock) {
        this.usuarioPort = usuarioPort;
        this.hasher = hasher;
        this.intentos = intentos;
        this.captcha = captcha;
        this.clock = clock;
    }

    public UsuarioSesion ejecutar(String usuario, String clave, String captchaEsperado, String captchaIngresado) {
        String clavePorLogin = usuario == null ? "" : usuario.trim().toLowerCase();
        Instant ahora = clock.instant();
        Optional<Instant> bloqueo = intentos.bloqueadoHasta(clavePorLogin);
        if (bloqueo.isPresent() && ahora.isBefore(bloqueo.get())) {
            long segs = Math.max(1L, (Duration.between(ahora, bloqueo.get()).toMillis() + 999) / 1000);
            throw new ReglaNegocioException("Demasiados intentos fallidos. Espera " + segs
                    + " segundos antes de volver a intentar.");
        }
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            fallo(clavePorLogin, ahora);
            throw new ReglaNegocioException("Ingresa usuario y contraseña.");
        }
        if (!captcha.validar(captchaEsperado, captchaIngresado)) {
            fallo(clavePorLogin, ahora);
            throw new ReglaNegocioException("Captcha inválido.");
        }
        Usuario encontrado = usuarioPort.findHabilitadoByLogin(usuario.trim())
                .filter(u -> hasher.verificar(clave, u.getPasswordHash()))
                .orElse(null);
        Optional<RolSistema> rol = encontrado == null ? Optional.empty() : RolSistema.desde(encontrado.getRol());
        if (encontrado == null || rol.isEmpty()) {
            fallo(clavePorLogin, ahora);
            throw new ReglaNegocioException("Usuario o contraseña incorrectos, o cuenta inactiva.");
        }
        intentos.reiniciar(clavePorLogin);
        return new UsuarioSesion(
                encontrado.getIdUsuario(),
                encontrado.getUsername(),
                encontrado.getNombreCompleto(),
                rol.get(),
                hasher.esClaveInicial(encontrado.getPasswordHash())
        );
    }

    private void fallo(String clavePorLogin, Instant ahora) {
        if (intentos.registrarFallo(clavePorLogin) >= MAX_INTENTOS_FALLIDOS) {
            intentos.bloquear(clavePorLogin, ahora.plus(BLOQUEO));
        }
    }
}
