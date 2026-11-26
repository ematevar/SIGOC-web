package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarCobranzaUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.PagoResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.ProcesarPagoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-22: Consulta de cobranzas y registro interno de pagos con ingreso financiero automático.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final GestionarCobranzaUseCase gestionarCobranza;

    public PagoController(GestionarCobranzaUseCase gestionarCobranza) {
        this.gestionarCobranza = gestionarCobranza;
    }

    @GetMapping
    public List<PagoResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return gestionarCobranza.listarPagos(actor, idObra).stream().map(PagoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse procesar(UsuarioSesion actor, @Valid @RequestBody ProcesarPagoRequest body) {
        return PagoResponse.de(gestionarCobranza.registrarPagoInterno(
                actor, body.idObra(), body.idValorizacion(), body.idComprobante(),
                body.monto(), body.metodoPago(), body.codigoOperacion()));
    }
}
