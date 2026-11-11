package com.sigoc.sigoc_web.infrastructure.adapters;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorReportePort;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exportador en memoria a CSV (UTF-8 BOM), Excel OpenXML (.xlsx) con ancho automático
 * y PDF 1.4 completo con OpenPDF sin truncamiento de columnas (RF-16, C20).
 */
@Component
public class ExportadorReporteAdapter implements ExportadorReportePort {

    @Override
    public byte[] exportar(Reporte reporte, FormatoExportacion formato) {
        return switch (formato) {
            case CSV -> exportarCsv(reporte);
            case XLSX -> exportarXlsx(reporte);
            case PDF -> exportarPdf(reporte);
        };
    }

    private byte[] exportarCsv(Reporte reporte) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(reporte.columnas().stream().map(this::csv).collect(Collectors.joining(","))).append("\n");
        for (List<Object> row : reporte.filas()) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(csv(row.get(i) == null ? "" : row.get(i).toString()));
            }
            sb.append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportarXlsx(Reporte reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            escribirEntradaZip(zos, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>");

            escribirEntradaZip(zos, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");

            escribirEntradaZip(zos, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"Reporte SIGOC\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                    + "</workbook>");

            escribirEntradaZip(zos, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "</Relationships>");

            List<String> cols = reporte.columnas();
            int[] anchos = new int[cols.size()];
            for (int c = 0; c < cols.size(); c++) {
                anchos[c] = Math.max(12, Math.min(48, cols.get(c).length() + 4));
            }
            for (List<Object> fila : reporte.filas()) {
                for (int c = 0; c < fila.size() && c < anchos.length; c++) {
                    String txt = fila.get(c) == null ? "" : fila.get(c).toString();
                    anchos[c] = Math.max(anchos[c], Math.min(54, txt.length() + 4));
                }
            }

            StringBuilder sheet = new StringBuilder();
            sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                 .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
            if (anchos.length > 0) {
                sheet.append("<cols>");
                for (int c = 0; c < anchos.length; c++) {
                    sheet.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                         .append("\" width=\"").append(anchos[c]).append("\" customWidth=\"1\"/>");
                }
                sheet.append("</cols>");
            }
            sheet.append("<sheetData>");

            sheet.append("<row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>")
                 .append(escaparXml(reporte.titulo()))
                 .append("</t></is></c></row>");

            sheet.append("<row r=\"2\">");
            for (int c = 0; c < cols.size(); c++) {
                String ref = columnaExcel(c) + "2";
                sheet.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                     .append(escaparXml(cols.get(c)))
                     .append("</t></is></c>");
            }
            sheet.append("</row>");

            int rowNum = 3;
            for (List<Object> fila : reporte.filas()) {
                sheet.append("<row r=\"").append(rowNum).append("\">");
                for (int c = 0; c < fila.size(); c++) {
                    String ref = columnaExcel(c) + rowNum;
                    Object val = fila.get(c);
                    if (val instanceof Number num) {
                        sheet.append("<c r=\"").append(ref).append("\" t=\"n\"><v>")
                             .append(num)
                             .append("</v></c>");
                    } else {
                        String txt = val == null ? "" : val.toString();
                        sheet.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                             .append(escaparXml(txt))
                             .append("</t></is></c>");
                    }
                }
                sheet.append("</row>");
                rowNum++;
            }
            sheet.append("</sheetData></worksheet>");
            escribirEntradaZip(zos, "xl/worksheets/sheet1.xml", sheet.toString());
            zos.finish();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Error generando archivo XLSX", ex);
        }
    }

    private byte[] exportarPdf(Reporte reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPdfVersion(PdfWriter.VERSION_1_4);
            doc.open();

            Font fTitulo = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(15, 23, 42));
            Font fHead = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
            Font fCell = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(15, 23, 42));

            Paragraph title = new Paragraph("SIGOC Web — " + reporte.titulo(), fTitulo);
            title.setSpacingAfter(10f);
            doc.add(title);

            List<String> cols = reporte.columnas();
            int nCols = Math.max(1, cols.size());
            PdfPTable table = new PdfPTable(nCols);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            for (String col : cols) {
                PdfPCell hc = new PdfPCell(new Phrase(col, fHead));
                hc.setBackgroundColor(new Color(30, 58, 138));
                hc.setPadding(5f);
                table.addCell(hc);
            }

            boolean par = false;
            for (List<Object> fila : reporte.filas()) {
                Color bg = par ? new Color(248, 250, 252) : Color.WHITE;
                for (int i = 0; i < nCols; i++) {
                    Object v = i < fila.size() ? fila.get(i) : "";
                    String txt = v == null ? "" : v.toString();
                    PdfPCell cell = new PdfPCell(new Phrase(txt, fCell));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(4.5f);
                    if (v instanceof Number) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }
                    table.addCell(cell);
                }
                par = !par;
            }
            if (reporte.filas().isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No hay registros para los filtros seleccionados.", fCell));
                empty.setColspan(nCols);
                empty.setPadding(8f);
                table.addCell(empty);
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Error generando PDF del reporte", ex);
        }
    }

    private void escribirEntradaZip(ZipOutputStream zos, String nombre, String contenido) throws IOException {
        zos.putNextEntry(new ZipEntry(nombre));
        zos.write(contenido.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String columnaExcel(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        while (n >= 0) {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        }
        return sb.toString();
    }

    private String escaparXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
