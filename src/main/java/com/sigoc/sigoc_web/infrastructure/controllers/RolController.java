package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.Rol;
import com.sigoc.sigoc_web.domain.ports.in.GestionarUsuarioUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CU-23: Consulta del catálogo de roles del sistema.
 */
@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final GestionarUsuarioUseCase gestionarUsuario;

    public RolController(GestionarUsuarioUseCase gestionarUsuario) {
        this.gestionarUsuario = gestionarUsuario;
    }

    @GetMapping
    public List<Rol> listar() {
        return gestionarUsuario.listarRoles();
    }
}
