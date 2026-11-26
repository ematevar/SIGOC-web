package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarComprobanteUseCase;
import com.sigoc.sigoc_web.domain.ports.in.GestionarValorizacionUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * CU-18 y CU-19: Valorizaciones automáticas, comprobantes internos CV01, PDF y enlaces de cobro.
 */
@RestController
@RequestMapping("/api/valorizaciones")
public class ValorizacionController {

    private final GestionarValorizacionUseCase gestionarValorizacion;
    private final GestionarComprobanteUseCase gestionarComprobante;

    public ValorizacionController(GestionarValorizacionUseCase gestionarValorizacion,
                                  GestionarComprobanteUseCase gestionarComprobante) {
        this.gestionarValorizacion = gestionarValorizacion;
        this.gestionarComprobante = gestionarComprobante;
    }

    @GetMapping
    public List<ValorizacionResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return gestionarValorizacion.listar(actor, idObra).stream().map(ValorizacionResponse::de).toList();
    }

    @GetMapping("/preview")
    public CalculoValorizacionResponse previsualizar(
            UsuarioSesion actor,
            @RequestParam Integer idObra,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoFin) {
        return CalculoValorizacionResponse.de(
                gestionarValorizacion.previsualizar(actor, idObra, periodoInicio, periodoFin));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ValorizacionResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarValorizacionRequest body) {
        return ValorizacionResponse.de(gestionarValorizacion.registrar(
                actor, body.idObra(), body.periodoInicio(), body.periodoFin(), body.fechaEmision()));
    }

    @GetMapping("/comprobantes")
    public List<ComprobanteResponse> listarComprobantes(UsuarioSesion actor) {
        return gestionarComprobante.listar(actor).stream().map(ComprobanteResponse::de).toList();
    }

    @PostMapping("/{idValorizacion}/comprobante")
    @ResponseStatus(HttpStatus.CREATED)
    public ComprobanteResponse emitirComprobante(UsuarioSesion actor,
                                                 @PathVariable Integer idValorizacion,
                                                 @RequestBody(required = false) EmitirComprobanteRequest body,
                                                 @RequestParam(required = false) String tipoComprobante,
                                                 @RequestParam(required = false) String serieNumero) {
        String tipo = body != null ? body.tipoComprobante() : tipoComprobante;
        String serie = body != null ? body.serieNumero() : serieNumero;
        return ComprobanteResponse.de(gestionarComprobante.emitir(actor, idValorizacion, tipo, serie));
    }

    @PostMapping("/comprobantes/{idComprobante}/renovar-enlace")
    public ComprobanteResponse renovarEnlace(UsuarioSesion actor, @PathVariable Integer idComprobante) {
        return ComprobanteResponse.de(gestionarComprobante.renovarEnlace(actor, idComprobante));
    }

    @GetMapping("/comprobantes/{idComprobante}/pdf")
    public ResponseEntity<byte[]> descargarPdf(UsuarioSesion actor, @PathVariable Integer idComprobante) {
        GestionarComprobanteUseCase.ResultadoPdf pdf = gestionarComprobante.generarPdf(actor, idComprobante);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.contenido());
    }
}
