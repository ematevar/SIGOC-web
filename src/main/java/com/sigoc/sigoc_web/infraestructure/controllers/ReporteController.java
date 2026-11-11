package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.ExportarReporte;
import com.sigoc.sigoc_web.aplication.usecases.GenerarReporte;
import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.models.TipoReporte;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * RF-16: reportes gerenciales (Curva S, Consumo, Personal, Financiero) y exportación CSV/XLSX/PDF (solo Admin).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final GenerarReporte generarReporte;
    private final ExportarReporte exportarReporte;

    public ReporteController(GenerarReporte generarReporte, ExportarReporte exportarReporte) {
        this.generarReporte = generarReporte;
        this.exportarReporte = exportarReporte;
    }

    @GetMapping
    public Reporte generar(UsuarioSesion actor,
                           @RequestParam String tipo,
                           @RequestParam(required = false) Integer idObra,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return generarReporte.ejecutar(actor, TipoReporte.desdeParametro(tipo), idObra, desde, hasta);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(UsuarioSesion actor,
                                           @RequestParam String tipo,
                                           @RequestParam String formato,
                                           @RequestParam(required = false) Integer idObra,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        TipoReporte tipoReporte = TipoReporte.desdeParametro(tipo);
        FormatoExportacion formatoExp = FormatoExportacion.desdeParametro(formato);
        byte[] contenido = exportarReporte.ejecutar(actor, tipoReporte, formatoExp, idObra, desde, hasta);

        String nombreArchivo = "reporte_" + tipoReporte.name().toLowerCase() + "." + formatoExp.getExtension();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(formatoExp.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }
}
