package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarEgresoUseCase;
import com.sigoc.sigoc_web.domain.ports.in.GestionarIngresoUseCase;
import com.sigoc.sigoc_web.domain.ports.in.GestionarPlanillaUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FinanzasDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * CU-13, CU-14, CU-15: Ingresos, previsualización y cierre inmutable de planillas, y egresos.
 */
@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    private final GestionarIngresoUseCase gestionarIngreso;
    private final GestionarEgresoUseCase gestionarEgreso;
    private final GestionarPlanillaUseCase gestionarPlanilla;

    public FinanzasController(GestionarIngresoUseCase gestionarIngreso,
                              GestionarEgresoUseCase gestionarEgreso,
                              GestionarPlanillaUseCase gestionarPlanilla) {
        this.gestionarIngreso = gestionarIngreso;
        this.gestionarEgreso = gestionarEgreso;
        this.gestionarPlanilla = gestionarPlanilla;
    }

    @GetMapping("/ingresos")
    public List<IngresoResponse> listarIngresos(UsuarioSesion actor,
                                                @RequestParam(required = false) Integer idObra) {
        return gestionarIngreso.listar(actor, idObra).stream().map(IngresoResponse::de).toList();
    }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoResponse registrarIngreso(UsuarioSesion actor, @Valid @RequestBody IngresoRequest body) {
        return IngresoResponse.de(gestionarIngreso.registrar(
                actor, body.idObra(), body.fecha(), body.concepto(), body.monto()));
    }

    @GetMapping("/egresos")
    public List<EgresoResponse> listarEgresos(UsuarioSesion actor,
                                              @RequestParam(required = false) Integer idObra) {
        return gestionarEgreso.listar(actor, idObra).stream().map(EgresoResponse::de).toList();
    }

    @PostMapping("/egresos")
    @ResponseStatus(HttpStatus.CREATED)
    public EgresoResponse registrarEgreso(UsuarioSesion actor, @Valid @RequestBody EgresoRequest body) {
        return EgresoResponse.de(gestionarEgreso.registrar(
                actor, body.idObra(), body.fecha(), body.concepto(), body.monto()));
    }

    @GetMapping("/planillas")
    public List<CierrePlanillaResponse> listarCierres(UsuarioSesion actor,
                                                      @RequestParam(required = false) Integer idObra) {
        return gestionarPlanilla.listarCierres(actor, idObra).stream().map(CierrePlanillaResponse::de).toList();
    }

    @GetMapping("/planillas/preview")
    public GestionarPlanillaUseCase.ResumenPlanilla previsualizarPlanilla(UsuarioSesion actor,
                                                                          @RequestParam Integer idObra,
                                                                          @RequestParam LocalDate desde,
                                                                          @RequestParam LocalDate hasta) {
        return gestionarPlanilla.previsualizar(actor, idObra, desde, hasta);
    }

    @PostMapping("/planillas/cerrar")
    @ResponseStatus(HttpStatus.CREATED)
    public CierrePlanillaResponse cerrarPlanilla(UsuarioSesion actor,
                                                 @Valid @RequestBody CerrarPlanillaRequest body) {
        return CierrePlanillaResponse.de(gestionarPlanilla.cerrar(
                actor, body.idObra(), body.desde(), body.hasta()));
    }
}
