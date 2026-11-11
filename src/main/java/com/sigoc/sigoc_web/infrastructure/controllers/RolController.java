package com.sigoc.sigoc_web.infrastructure.controllers;


import com.sigoc.sigoc_web.application.usecases.ListarRoles;
import com.sigoc.sigoc_web.domain.models.Rol;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    private final ListarRoles listarRoles;

    //Inyeccion del caso de uso

    public RolController(ListarRoles listarRoles) {
        this.listarRoles = listarRoles;
    }

    @GetMapping
    public List<Rol> listar() {
        return listarRoles.ejecutar();
    }
}
