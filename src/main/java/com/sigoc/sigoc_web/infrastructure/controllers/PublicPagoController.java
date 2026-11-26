package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.ports.in.GestionarCobranzaUseCase;
import com.sigoc.sigoc_web.domain.ports.in.GestionarCobranzaUseCase.DetalleEnlacePublico;
import com.sigoc.sigoc_web.domain.ports.in.GestionarComprobanteUseCase;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoExternoUseCase;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoExternoUseCase.OrdenPagoExterno;
import com.sigoc.sigoc_web.domain.ports.in.ProcesarPagoTarjetaUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.PagoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * CU-19, CU-20, CU-22, CU-24: Portal público de pago identificado por token de 72 horas.
 */
@RestController
@RequestMapping("/api/public/pagos/{codigoPago}")
public class PublicPagoController {

    private final GestionarCobranzaUseCase gestionarCobranza;
    private final ProcesarPagoExternoUseCase procesarPagoExterno;
    private final ProcesarPagoTarjetaUseCase procesarPagoTarjeta;
    private final GestionarComprobanteUseCase gestionarComprobante;

    public PublicPagoController(GestionarCobranzaUseCase gestionarCobranza,
                                ProcesarPagoExternoUseCase procesarPagoExterno,
                                ProcesarPagoTarjetaUseCase procesarPagoTarjeta,
                                GestionarComprobanteUseCase gestionarComprobante) {
        this.gestionarCobranza = gestionarCobranza;
        this.procesarPagoExterno = procesarPagoExterno;
        this.procesarPagoTarjeta = procesarPagoTarjeta;
        this.gestionarComprobante = gestionarComprobante;
    }

    @GetMapping
    public DetalleEnlacePublico detalle(@PathVariable String codigoPago) {
        return gestionarCobranza.consultarEnlacePublico(codigoPago);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdfPublico(@PathVariable String codigoPago) {
        GestionarComprobanteUseCase.ResultadoPdf pdf = gestionarComprobante.generarPdfPublico(codigoPago);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.contenido());
    }

    @PostMapping("/simulada")
    public PagoResponse pagarTarjetaSimulada(@PathVariable String codigoPago,
                                             @Valid @RequestBody CobroTarjetaRequest body) {
        return PagoResponse.de(procesarPagoTarjeta.cobrarTarjetaPublica(
                codigoPago, body.numeroTarjeta(), body.titular(), body.vencimiento(), body.cvv()));
    }

    @PostMapping("/paypal/orden")
    public OrdenPagoExterno crearOrdenPayPal(@PathVariable String codigoPago) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/pago/{codigoPago}")
                .buildAndExpand(codigoPago)
                .toUriString();
        return procesarPagoExterno.crearOrdenPublica(
                codigoPago,
                baseUrl + "?paypalResultado=ok",
                baseUrl + "?paypalResultado=cancelado");
    }

    @PostMapping("/paypal/capturar")
    public PagoResponse capturarOrdenPayPal(@PathVariable String codigoPago,
                                            @Valid @RequestBody CapturaPayPalRequest body) {
        return PagoResponse.de(procesarPagoExterno.capturarPublico(codigoPago, body.idOrdenExterna()));
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
