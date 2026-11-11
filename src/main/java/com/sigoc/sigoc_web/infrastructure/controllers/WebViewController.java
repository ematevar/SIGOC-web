package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.security.SesionHttp;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de vistas web Thymeleaf (login y aplicación principal).
 */
@Controller
public class WebViewController {

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        if (SesionHttp.usuario(request) != null) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping({"/", "/index"})
    public String index(HttpServletRequest request, Model model) {
        UsuarioSesion usuario = SesionHttp.usuario(request);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", usuario.username());
        model.addAttribute("nombreCompleto", usuario.nombreCompleto());
        model.addAttribute("rol", usuario.rol().name());
        model.addAttribute("rolEtiqueta", usuario.rol().getEtiqueta());
        model.addAttribute("requiereCambioClave", usuario.requiereCambioClave());
        return "index";
    }
}
