package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.*;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FinanzasDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-13, RF-14, RF-15: ingresos, cierre inmutable de planillas y egresos (solo Administrador).
 */
@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    private final ListarIngresos listarIngresos;
    private final RegistrarIngreso registrarIngreso;
    private final ListarEgresos listarEgresos;
    private final RegistrarEgreso registrarEgreso;
    private final ListarCierresPlanilla listarCierresPlanilla;
    private final CerrarPlanilla cerrarPlanilla;

    public FinanzasController(ListarIngresos listarIngresos, RegistrarIngreso registrarIngreso,
                              ListarEgresos listarEgresos, RegistrarEgreso registrarEgreso,
                              ListarCierresPlanilla listarCierresPlanilla, CerrarPlanilla cerrarPlanilla) {
        this.listarIngresos = listarIngresos;
        this.registrarIngreso = registrarIngreso;
        this.listarEgresos = listarEgresos;
        this.registrarEgreso = registrarEgreso;
        this.listarCierresPlanilla = listarCierresPlanilla;
        this.cerrarPlanilla = cerrarPlanilla;
    }

    @GetMapping("/ingresos")
    public List<IngresoResponse> listarIngresos(UsuarioSesion actor,
                                                @RequestParam(required = false) Integer idObra) {
        return listarIngresos.ejecutar(actor, idObra).stream().map(IngresoResponse::de).toList();
    }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoResponse registrarIngreso(UsuarioSesion actor, @Valid @RequestBody IngresoRequest body) {
        return IngresoResponse.de(registrarIngreso.ejecutar(
                actor, body.idObra(), body.fecha(), body.concepto(), body.monto()));
    }

    @GetMapping("/egresos")
    public List<EgresoResponse> listarEgresos(UsuarioSesion actor,
                                              @RequestParam(required = false) Integer idObra) {
        return listarEgresos.ejecutar(actor, idObra).stream().map(EgresoResponse::de).toList();
    }

    @PostMapping("/egresos")
    @ResponseStatus(HttpStatus.CREATED)
    public EgresoResponse registrarEgreso(UsuarioSesion actor, @Valid @RequestBody EgresoRequest body) {
        return EgresoResponse.de(registrarEgreso.ejecutar(
                actor, body.idObra(), body.fecha(), body.concepto(), body.monto()));
    }

    @GetMapping("/planillas")
    public List<CierrePlanillaResponse> listarCierres(UsuarioSesion actor,
                                                      @RequestParam(required = false) Integer idObra) {
        return listarCierresPlanilla.ejecutar(actor, idObra).stream().map(CierrePlanillaResponse::de).toList();
    }

    @PostMapping("/planillas/cerrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CierrePlanillaResponse cerrarPlanilla(UsuarioSesion actor,
                                                 @Valid @RequestBody CerrarPlanillaRequest body) {
        return CierrePlanillaResponse.de(cerrarPlanilla.ejecutar(
                actor, body.idObra(), body.desde(), body.hasta()));
    }
}
