package com.sigoc.sigoc_web.infrastructure.adapters;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorComprobantePdfPort;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Genera el comprobante interno de valorización CV01 en formato PDF usando OpenPDF (RF-19).
 */
@Component
public class ComprobantePdfAdapter implements ExportadorComprobantePdfPort {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generarPdf(Comprobante comp, Valorizacion val, Obra obra, Cliente cliente) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo = new Font(Font.HELVETICA, 15, Font.BOLD, new Color(15, 23, 42));
            Font fSubtitulo = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(37, 99, 235));
            Font fBanner = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(153, 27, 27));
            Font fEtiqueta = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(51, 65, 85));
            Font fTexto = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(15, 23, 42));
            Font fTotal = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42));

            Paragraph header = new Paragraph("SIGOC Web — Sistema de Gestión de Obras Civiles", fTitulo);
            doc.add(header);
            Paragraph sub = new Paragraph("COMPROBANTE INTERNO DE VALORIZACIÓN DE OBRA: " + comp.getSerieNumero(), fSubtitulo);
            sub.setSpacingAfter(8f);
            doc.add(sub);

            PdfPTable bannerTbl = new PdfPTable(1);
            bannerTbl.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell(new Phrase(
                    "DOCUMENTO INTERNO DE COBRANZA Y CONTROL PRESUPUESTAL — SIN VALOR TRIBUTARIO SUNAT", fBanner));
            bannerCell.setBackgroundColor(new Color(254, 242, 242));
            bannerCell.setBorderColor(new Color(248, 113, 113));
            bannerCell.setPadding(7f);
            bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bannerTbl.addCell(bannerCell);
            bannerTbl.setSpacingAfter(12f);
            doc.add(bannerTbl);

            PdfPTable datos = new PdfPTable(new float[]{1.2f, 2.2f, 1.2f, 2.0f});
            datos.setWidthPercentage(100);
            datos.setSpacingAfter(12f);

            agregarPar(datos, "Comprobante:", comp.getSerieNumero(), fEtiqueta, fTexto);
            agregarPar(datos, "Fecha emisión:", comp.getFechaEmision() != null ? comp.getFechaEmision().format(FECHA_FMT) : "-", fEtiqueta, fTexto);
            agregarPar(datos, "Estado:", comp.getEstado(), fEtiqueta, fTexto);
            agregarPar(datos, "Vigencia enlace:", comp.getVenceEl() != null ? comp.getVenceEl().format(FECHA_HORA_FMT) : "72 horas", fEtiqueta, fTexto);

            String nombreObra = obra != null ? obra.getNombre() : "Obra #" + (val != null ? val.getIdObra() : "-");
            String nombreCliente = cliente != null
                    ? cliente.getNombreRazonSocial() + " (" + cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento() + ")"
                    : (obra != null && obra.getCliente() != null ? obra.getCliente() : "No especificado");
            agregarPar(datos, "Obra:", nombreObra, fEtiqueta, fTexto);
            agregarPar(datos, "Valorización N°:", val != null && val.getNumero() != null ? "N° " + val.getNumero() : "-", fEtiqueta, fTexto);
            agregarPar(datos, "Cliente:", nombreCliente, fEtiqueta, fTexto);
            agregarPar(datos, "Periodo:", val != null ? val.getPeriodo() : "-", fEtiqueta, fTexto);

            doc.add(datos);

            PdfPTable detalle = new PdfPTable(new float[]{3.5f, 1.3f, 1.3f});
            detalle.setWidthPercentage(100);
            detalle.setSpacingAfter(12f);
            agregarCabecera(detalle, "Concepto", fEtiqueta);
            agregarCabecera(detalle, "Avance %", fEtiqueta);
            agregarCabecera(detalle, "Importe (S/)", fEtiqueta);

            String descVal = "Valorización de obra N° " + (val != null && val.getNumero() != null ? val.getNumero() : 1)
                    + (val != null && val.getPeriodo() != null ? " — Periodo " + val.getPeriodo() : "")
                    + (val != null && val.getAvanceAcumulado() != null ? " (Acum. " + val.getAvanceAcumulado() + "%)" : "");
            PdfPCell cDesc = new PdfPCell(new Phrase(descVal, fTexto));
            cDesc.setPadding(6f);
            detalle.addCell(cDesc);

            PdfPCell cPct = new PdfPCell(new Phrase(
                    val != null && val.getPorcentajeAvancePeriodo() != null ? val.getPorcentajeAvancePeriodo() + " %" : "-", fTexto));
            cPct.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cPct.setPadding(6f);
            detalle.addCell(cPct);

            PdfPCell cSub = new PdfPCell(new Phrase("S/ " + fmt(comp.getSubtotal()), fTexto));
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cSub.setPadding(6f);
            detalle.addCell(cSub);

            doc.add(detalle);

            PdfPTable totales = new PdfPTable(new float[]{4.2f, 1.8f});
            totales.setWidthPercentage(55);
            totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totales.setSpacingAfter(14f);
            agregarFilaTotal(totales, "Subtotal valorizado:", "S/ " + fmt(comp.getSubtotal()), fEtiqueta, fTexto);
            agregarFilaTotal(totales, "IGV (18%):", "S/ " + fmt(comp.getIgv()), fEtiqueta, fTexto);
            agregarFilaTotal(totales, "TOTAL A PAGAR:", "S/ " + fmt(comp.getTotal()), fTotal, fTotal);
            doc.add(totales);

            if (comp.getCodigoPago() != null && !comp.getCodigoPago().isBlank()) {
                Paragraph pLink = new Paragraph(
                        "Código público de pago (vigencia 72 horas): /pago/" + comp.getCodigoPago(), fTexto);
                pLink.setSpacingBefore(6f);
                doc.add(pLink);
            }

            doc.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void agregarPar(PdfPTable t, String k, String v, Font fk, Font fv) {
        PdfPCell ck = new PdfPCell(new Phrase(k, fk));
        ck.setBackgroundColor(new Color(241, 245, 249));
        ck.setPadding(5f);
        t.addCell(ck);
        PdfPCell cv = new PdfPCell(new Phrase(v != null ? v : "-", fv));
        cv.setPadding(5f);
        t.addCell(cv);
    }

    private void agregarCabecera(PdfPTable t, String texto, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBackgroundColor(new Color(226, 232, 240));
        c.setPadding(6f);
        t.addCell(c);
    }

    private void agregarFilaTotal(PdfPTable t, String k, String v, Font fk, Font fv) {
        PdfPCell ck = new PdfPCell(new Phrase(k, fk));
        ck.setPadding(5f);
        ck.setBorder(Rectangle.NO_BORDER);
        t.addCell(ck);
        PdfPCell cv = new PdfPCell(new Phrase(v, fv));
        cv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cv.setPadding(5f);
        cv.setBorder(Rectangle.BOTTOM);
        t.addCell(cv);
    }

    private String fmt(BigDecimal n) {
        return n == null ? "0.00" : String.format("%,.2f", n);
    }
}
