package com.sigoc.sigoc_web.infraestructure.config;

import com.sigoc.sigoc_web.aplication.usecases.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
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

    // ---------- Obras y partidas (RF-03, RF-04, RF-16b) ----------

    @Bean
    public ListarObras listarObras(ObraPort obraPort) {
        return new ListarObras(obraPort);
    }

    @Bean
    public GuardarObra guardarObra(ObraPort obraPort) {
        return new GuardarObra(obraPort);
    }

    @Bean
    public EliminarObra eliminarObra(ObraPort obraPort) {
        return new EliminarObra(obraPort);
    }

    @Bean
    public CerrarObra cerrarObra(ObraPort obraPort) {
        return new CerrarObra(obraPort);
    }

    @Bean
    public ListarPartidas listarPartidas(PartidaPort partidaPort) {
        return new ListarPartidas(partidaPort);
    }

    @Bean
    public GuardarPartida guardarPartida(PartidaPort partidaPort, ObraPort obraPort) {
        return new GuardarPartida(partidaPort, obraPort);
    }

    @Bean
    public EliminarPartida eliminarPartida(PartidaPort partidaPort) {
        return new EliminarPartida(partidaPort);
    }

    @Bean
    public ListarMaterialesPartida listarMaterialesPartida(PartidaPort partidaPort,
                                                           PartidaMaterialPort partidaMaterialPort) {
        return new ListarMaterialesPartida(partidaPort, partidaMaterialPort);
    }

    @Bean
    public AsignarMaterialPartida asignarMaterialPartida(PartidaPort partidaPort, ObraPort obraPort,
                                                         PartidaMaterialPort partidaMaterialPort) {
        return new AsignarMaterialPartida(partidaPort, obraPort, partidaMaterialPort);
    }

    @Bean
    public EliminarMaterialPartida eliminarMaterialPartida(PartidaMaterialPort partidaMaterialPort) {
        return new EliminarMaterialPartida(partidaMaterialPort);
    }

    // ---------- Avances físicos (RF-05) ----------

    @Bean
    public ObtenerAvanceObra obtenerAvanceObra(ObraPort obraPort, PartidaPort partidaPort,
                                               AvancePartidaPort avancePort) {
        return new ObtenerAvanceObra(obraPort, partidaPort, avancePort);
    }

    @Bean
    public RegistrarAvance registrarAvance(AvancePartidaPort avancePort, PartidaPort partidaPort,
                                           ObraPort obraPort, ObtenerAvanceObra obtenerAvanceObra, Clock clock) {
        return new RegistrarAvance(avancePort, partidaPort, obraPort, obtenerAvanceObra, clock);
    }

    @Bean
    public ListarAvances listarAvances(AvancePartidaPort avancePort) {
        return new ListarAvances(avancePort);
    }
}
