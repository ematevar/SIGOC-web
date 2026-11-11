package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarPagos;
import com.sigoc.sigoc_web.application.usecases.ProcesarPago;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.PagoResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.ProcesarPagoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pasarela sandbox de pagos con registro automático de ingreso financiero en la obra.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final ListarPagos listarPagos;
    private final ProcesarPago procesarPago;

    public PagoController(ListarPagos listarPagos, ProcesarPago procesarPago) {
        this.listarPagos = listarPagos;
        this.procesarPago = procesarPago;
    }

    @GetMapping
    public List<PagoResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarPagos.ejecutar(actor, idObra).stream().map(PagoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PagoResponse procesar(UsuarioSesion actor, @Valid @RequestBody ProcesarPagoRequest body) {
        return PagoResponse.de(procesarPago.ejecutar(
                actor, body.idObra(), body.idValorizacion(), body.idComprobante(),
                body.monto(), body.metodoPago(), body.codigoOperacion()));
    }
}
