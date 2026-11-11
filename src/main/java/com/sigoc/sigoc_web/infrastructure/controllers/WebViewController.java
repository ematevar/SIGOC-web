package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.infrastructure.security.SesionHttp;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Controlador de vistas web Thymeleaf (login y aplicación principal).
 */
@Controller
public class WebViewController {

    private final UsuarioPort usuarioPort;

    public WebViewController() {
        this.usuarioPort = null;
    }

    @Autowired
    public WebViewController(UsuarioPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        UsuarioSesion usuario = sesionValida(request);
        if (usuario != null) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model) {
        UsuarioSesion usuario = sesionValida(request);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", usuario.username());
        model.addAttribute("nombreCompleto", usuario.nombreCompleto());
        model.addAttribute("rol", usuario.rol().name());
        model.addAttribute("rolEtiqueta", usuario.rol().getEtiqueta());
        model.addAttribute("requiereCambioClave", usuario.requiereCambioClave());
        model.addAttribute("csrfToken", SesionHttp.obtenerOCrearCsrfToken(request));
        return "index";
    }

    @GetMapping("/pago/{codigoPago}")
    public String portalPagoPublico(@PathVariable String codigoPago, Model model) {
        model.addAttribute("codigoPago", codigoPago);
        return "pago-publico";
    }

    private UsuarioSesion sesionValida(HttpServletRequest request) {
        UsuarioSesion sesion = SesionHttp.usuario(request);
        if (sesion == null) {
            return null;
        }
        if (usuarioPort == null) {
            return sesion;
        }
        Optional<Usuario> db = usuarioPort.findHabilitadoByLogin(sesion.username());
        if (db.isEmpty() || !db.get().getIdUsuario().equals(sesion.idUsuario())) {
            SesionHttp.invalidar(request);
            return null;
        }
        boolean mismoRol = RolSistema.desde(db.get().getRol()).map(r -> r == sesion.rol()).orElse(false);
        if (!mismoRol) {
            SesionHttp.invalidar(request);
            return null;
        }
        return sesion;
    }
}
