package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ConsultarEnlacePagoPublico;
import com.sigoc.sigoc_web.application.usecases.ConsultarEnlacePagoPublico.DetalleEnlacePublico;
import com.sigoc.sigoc_web.application.usecases.GenerarPdfComprobante;
import com.sigoc.sigoc_web.application.usecases.ProcesarPago;
import com.sigoc.sigoc_web.application.usecases.ProcesarPago.OrdenPayPalPublica;
import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.PagoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Portal público de pago (sin login interno) identificado por token de 72 horas.
 */
@RestController
@RequestMapping("/api/public/pagos/{codigoPago}")
public class PublicPagoController {

    private final ConsultarEnlacePagoPublico consultarEnlace;
    private final ProcesarPago procesarPago;
    private final GenerarPdfComprobante generarPdfComprobante;

    public PublicPagoController(ConsultarEnlacePagoPublico consultarEnlace,
                                ProcesarPago procesarPago,
                                GenerarPdfComprobante generarPdfComprobante) {
        this.consultarEnlace = consultarEnlace;
        this.procesarPago = procesarPago;
        this.generarPdfComprobante = generarPdfComprobante;
    }

    @GetMapping
    public DetalleEnlacePublico detalle(@PathVariable String codigoPago) {
        return consultarEnlace.ejecutar(codigoPago);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdfPublico(@PathVariable String codigoPago) {
        GenerarPdfComprobante.ResultadoPdf pdf = generarPdfComprobante.ejecutarPublico(codigoPago);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.contenido());
    }

    @PostMapping("/simulada")
    @Transactional(noRollbackFor = PagoRechazadoException.class)
    public PagoResponse pagarTarjetaSimulada(@PathVariable String codigoPago,
                                             @Valid @RequestBody CobroTarjetaRequest body) {
        return PagoResponse.de(procesarPago.cobrarTarjetaSimuladaPublica(
                codigoPago, body.numeroTarjeta(), body.titular(), body.vencimiento(), body.cvv()));
    }

    @PostMapping("/paypal/orden")
    public OrdenPayPalPublica crearOrdenPayPal(@PathVariable String codigoPago) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/pago/{codigoPago}")
                .buildAndExpand(codigoPago)
                .toUriString();
        return procesarPago.crearOrdenPayPalPublica(
                codigoPago,
                baseUrl + "?paypalResultado=ok",
                baseUrl + "?paypalResultado=cancelado");
    }

    @PostMapping("/paypal/capturar")
    @Transactional(noRollbackFor = PagoRechazadoException.class)
    public PagoResponse capturarOrdenPayPal(@PathVariable String codigoPago,
                                            @Valid @RequestBody CapturaPayPalRequest body) {
        return PagoResponse.de(procesarPago.capturarPayPalPublico(codigoPago, body.idOrdenExterna()));
    }

    public record CobroTarjetaRequest(
            @NotBlank String numeroTarjeta,
            @NotBlank String titular,
            @NotBlank String vencimiento,
            @NotBlank String cvv
    ) {
    }

    public record CapturaPayPalRequest(@NotBlank String idOrdenExterna) {
    }
}
