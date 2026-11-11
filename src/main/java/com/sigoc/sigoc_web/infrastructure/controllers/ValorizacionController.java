package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.EmitirComprobante;
import com.sigoc.sigoc_web.application.usecases.ListarComprobantes;
import com.sigoc.sigoc_web.application.usecases.ListarValorizaciones;
import com.sigoc.sigoc_web.application.usecases.RegistrarValorizacion;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de valorizaciones de obra y emisión de comprobantes de prueba.
 */
@RestController
@RequestMapping("/api/valorizaciones")
public class ValorizacionController {

    private final ListarValorizaciones listarValorizaciones;
    private final RegistrarValorizacion registrarValorizacion;
    private final ListarComprobantes listarComprobantes;
    private final EmitirComprobante emitirComprobante;

    public ValorizacionController(ListarValorizaciones listarValorizaciones,
                                  RegistrarValorizacion registrarValorizacion,
                                  ListarComprobantes listarComprobantes,
                                  EmitirComprobante emitirComprobante) {
        this.listarValorizaciones = listarValorizaciones;
        this.registrarValorizacion = registrarValorizacion;
        this.listarComprobantes = listarComprobantes;
        this.emitirComprobante = emitirComprobante;
    }

    @GetMapping
    public List<ValorizacionResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarValorizaciones.ejecutar(actor, idObra).stream().map(ValorizacionResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValorizacionResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarValorizacionRequest body) {
        return ValorizacionResponse.de(registrarValorizacion.ejecutar(
                actor, body.idObra(), body.periodo(), body.fechaEmision(),
                body.porcentajeAvancePeriodo(), body.montoValorizado()));
    }

    @GetMapping("/comprobantes")
    public List<ComprobanteResponse> listarComprobantes(UsuarioSesion actor) {
        return listarComprobantes.ejecutar(actor).stream().map(ComprobanteResponse::de).toList();
    }

    @PostMapping("/{idValorizacion}/comprobante")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ComprobanteResponse emitirComprobante(UsuarioSesion actor,
                                                   @PathVariable Integer idValorizacion,
                                                   @RequestBody(required = false) EmitirComprobanteRequest body,
                                                   @RequestParam(required = false) String tipoComprobante,
                                                   @RequestParam(required = false) String serieNumero) {
        String tipo = body != null ? body.tipoComprobante() : tipoComprobante;
        String serie = body != null ? body.serieNumero() : serieNumero;
        return ComprobanteResponse.de(emitirComprobante.ejecutar(actor, idValorizacion, tipo, serie));
    }
}
