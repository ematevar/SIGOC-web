package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.security.SesionHttp;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.*;

class WebViewControllerTest {

    private final WebViewController controller = new WebViewController();

    @Test
    void sinSesionRedirigeALoginYMuestraVistaLogin() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertEquals("redirect:/login", controller.index(req, new ConcurrentModel()));
        assertEquals("login", controller.login(req));
    }

    @Test
    void conSesionCargaModeloYRedirigeLoginAInicio() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        SesionHttp.guardarUsuario(req, new UsuarioSesion(1, "admin", "Admin Demo", RolSistema.ADMIN, true));

        ConcurrentModel model = new ConcurrentModel();
        assertEquals("index", controller.index(req, model));
        assertEquals("admin", model.getAttribute("username"));
        assertEquals("Admin Demo", model.getAttribute("nombreCompleto"));
        assertEquals("ADMIN", model.getAttribute("rol"));
        assertEquals(true, model.getAttribute("requiereCambioClave"));
        assertEquals("redirect:/", controller.login(req));
    }
}
