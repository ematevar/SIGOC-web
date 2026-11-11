package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.*;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de valorizaciones automáticas, comprobantes internos CV01, PDF y enlaces de cobro.
 */
@RestController
@RequestMapping("/api/valorizaciones")
public class ValorizacionController {

    private final ListarValorizaciones listarValorizaciones;
    private final RegistrarValorizacion registrarValorizacion;
    private final ListarComprobantes listarComprobantes;
    private final EmitirComprobante emitirComprobante;
    private final GenerarPdfComprobante generarPdfComprobante;

    public ValorizacionController(ListarValorizaciones listarValorizaciones,
                                  RegistrarValorizacion registrarValorizacion,
                                  ListarComprobantes listarComprobantes,
                                  EmitirComprobante emitirComprobante,
                                  GenerarPdfComprobante generarPdfComprobante) {
        this.listarValorizaciones = listarValorizaciones;
        this.registrarValorizacion = registrarValorizacion;
        this.listarComprobantes = listarComprobantes;
        this.emitirComprobante = emitirComprobante;
        this.generarPdfComprobante = generarPdfComprobante;
    }

    @GetMapping
    public List<ValorizacionResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarValorizaciones.ejecutar(actor, idObra).stream().map(ValorizacionResponse::de).toList();
    }

    @GetMapping("/preview")
    public CalculoValorizacionResponse previsualizar(
            UsuarioSesion actor,
            @RequestParam Integer idObra,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoFin) {
        return CalculoValorizacionResponse.de(
                registrarValorizacion.previsualizar(actor, idObra, periodoInicio, periodoFin));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ValorizacionResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarValorizacionRequest body) {
        return ValorizacionResponse.de(registrarValorizacion.ejecutar(
                actor, body.idObra(), body.periodoInicio(), body.periodoFin(), body.fechaEmision()));
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

    @PostMapping("/comprobantes/{idComprobante}/renovar-enlace")
    @Transactional
    public ComprobanteResponse renovarEnlace(UsuarioSesion actor, @PathVariable Integer idComprobante) {
        return ComprobanteResponse.de(emitirComprobante.renovarEnlace(actor, idComprobante));
    }

    @GetMapping("/comprobantes/{idComprobante}/pdf")
    public ResponseEntity<byte[]> descargarPdf(UsuarioSesion actor, @PathVariable Integer idComprobante) {
        GenerarPdfComprobante.ResultadoPdf pdf = generarPdfComprobante.ejecutar(actor, idComprobante);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.contenido());
    }
}
