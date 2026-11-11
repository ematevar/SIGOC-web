package com.sigoc.sigoc_web.infraestructure.config;

import com.sigoc.sigoc_web.aplication.usecases.*;
import com.sigoc.sigoc_web.domain.ports.out.IntentosLoginPort;
import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.domain.services.CaptchaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CasosUsoConfig {

    // ---------- Infraestructura compartida ----------

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CaptchaService captchaService() {
        return new CaptchaService();
    }

    // ---------- Seguridad y usuarios (RF-01, RF-02) ----------

    @Bean
    public ListarRoles listarRoles(RolPort port) {
        return new ListarRoles(port);
    }

    @Bean
    public GenerarCaptcha generarCaptcha(CaptchaService captcha) {
        return new GenerarCaptcha(captcha);
    }

    @Bean
    public IniciarSesion iniciarSesion(UsuarioPort usuarioPort, PasswordHasherPort hasher,
                                       IntentosLoginPort intentos, CaptchaService captcha, Clock clock) {
        return new IniciarSesion(usuarioPort, hasher, intentos, captcha, clock);
    }

    @Bean
    public CambiarClave cambiarClave(UsuarioPort usuarioPort, PasswordHasherPort hasher) {
        return new CambiarClave(usuarioPort, hasher);
    }

    @Bean
    public ListarUsuarios listarUsuarios(UsuarioPort usuarioPort) {
        return new ListarUsuarios(usuarioPort);
    }

    @Bean
    public CrearUsuario crearUsuario(UsuarioPort usuarioPort, RolPort rolPort, PasswordHasherPort hasher) {
        return new CrearUsuario(usuarioPort, rolPort, hasher);
    }

    @Bean
    public ActualizarUsuario actualizarUsuario(UsuarioPort usuarioPort, RolPort rolPort) {
        return new ActualizarUsuario(usuarioPort, rolPort);
    }

    @Bean
    public CambiarEstadoUsuario cambiarEstadoUsuario(UsuarioPort usuarioPort) {
        return new CambiarEstadoUsuario(usuarioPort);
    }
}
