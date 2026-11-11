package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.FormatoExportacion;
import com.sigoc.sigoc_web.domain.models.Reporte;
import com.sigoc.sigoc_web.domain.ports.out.ExportadorReportePort;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exportador puro en memoria a CSV (UTF-8 BOM), Excel OpenXML (.xlsx) y PDF 1.4 estándar (RF-16).
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

            StringBuilder sheet = new StringBuilder();
            sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                 .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");

            sheet.append("<row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>")
                 .append(escaparXml(reporte.titulo()))
                 .append("</t></is></c></row>");

            sheet.append("<row r=\"2\">");
            List<String> cols = reporte.columnas();
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
        List<List<Object>> filas = reporte.filas();
        int filasPorPagina = 34;
        int totalPaginas = Math.max(1, (filas.size() + filasPorPagina - 1) / filasPorPagina);

        List<byte[]> streamsPaginas = new ArrayList<>();
        for (int p = 0; p < totalPaginas; p++) {
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n/F1 13 Tf\n36 555 Td\n(")
                  .append(escaparPdf("SIGOC Web - " + reporte.titulo() + " (Pag. " + (p + 1) + "/" + totalPaginas + ")"))
                  .append(") Tj\nET\n");

            stream.append("BT\n/F1 8 Tf\n36 535 Td\n(")
                  .append(escaparPdf(formatearFilaPdf(reporte.columnas())))
                  .append(") Tj\nET\n");

            int y = 518;
            int inicio = p * filasPorPagina;
            int fin = Math.min(filas.size(), inicio + filasPorPagina);
            for (int r = inicio; r < fin; r++) {
                stream.append("BT\n/F1 8 Tf\n36 ").append(y).append(" Td\n(")
                      .append(escaparPdf(formatearFilaPdf(filas.get(r))))
                      .append(") Tj\nET\n");
                y -= 14;
            }
            streamsPaginas.add(stream.toString().getBytes(StandardCharsets.ISO_8859_1));
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        escribirAscii(pdf, "%PDF-1.4\n");

        offsets.add(pdf.size());
        escribirAscii(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder kids = new StringBuilder("[");
        for (int p = 0; p < totalPaginas; p++) {
            if (p > 0) {
                kids.append(" ");
            }
            kids.append(4 + (p * 2)).append(" 0 R");
        }
        kids.append("]");

        offsets.add(pdf.size());
        escribirAscii(pdf, "2 0 obj\n<< /Type /Pages /Kids " + kids + " /Count " + totalPaginas + " >>\nendobj\n");

        offsets.add(pdf.size());
        escribirAscii(pdf, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");

        for (int p = 0; p < totalPaginas; p++) {
            int pageObjId = 4 + (p * 2);
            int contentObjId = pageObjId + 1;
            byte[] contentBytes = streamsPaginas.get(p);

            offsets.add(pdf.size());
            escribirAscii(pdf, pageObjId + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObjId + " 0 R >>\nendobj\n");

            offsets.add(pdf.size());
            escribirAscii(pdf, contentObjId + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
            pdf.writeBytes(contentBytes);
            escribirAscii(pdf, "\nendstream\nendobj\n");
        }

        int totalObjs = offsets.size() + 1;
        int xrefOffset = pdf.size();
        escribirAscii(pdf, "xref\n0 " + totalObjs + "\n0000000000 65535 f \n");
        for (int off : offsets) {
            escribirAscii(pdf, String.format("%010d 00000 n \n", off));
        }
        escribirAscii(pdf, "trailer\n<< /Size " + totalObjs + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");
        return pdf.toByteArray();
    }

    private String formatearFilaPdf(List<?> celdas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            String s = celdas.get(i) == null ? "" : celdas.get(i).toString();
            if (s.length() > 18) {
                s = s.substring(0, 18);
            }
            sb.append(String.format("%-18s", s));
        }
        return sb.toString();
    }

    private String escaparPdf(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private void escribirAscii(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
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
