package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.ConsultarAuditoriaUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CU-25: Consulta en solo lectura de la bitácora inmutable de auditoría (GERENTE).
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final ConsultarAuditoriaUseCase consultarAuditoriaUseCase;

    public AuditoriaController(ConsultarAuditoriaUseCase consultarAuditoriaUseCase) {
        this.consultarAuditoriaUseCase = consultarAuditoriaUseCase;
    }

    @GetMapping
    public List<AuditoriaEvento> listar(UsuarioSesion actor,
                                        @RequestParam(required = false) String accion,
                                        @RequestParam(required = false, defaultValue = "100") Integer limite) {
        return consultarAuditoriaUseCase.ejecutar(actor, accion, limite);
    }
}
