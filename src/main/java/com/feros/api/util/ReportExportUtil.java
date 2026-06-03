package com.feros.api.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReportExportUtil {

    private ReportExportUtil() {}

    public static byte[] toCsv(String[] headers, List<String[]> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(headers);
            rows.forEach(writer::writeNext);
            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    public static byte[] toPdf(String title, String[] headers, List<String[]> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.decode("#1E3A5F"));
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_LEFT);
            titlePara.setSpacingAfter(12f);
            doc.add(titlePara);

            // Table
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(6f);

            // Header row
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Color headerBg = Color.decode("#1E3A5F");
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(5f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Data rows
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);
            Color altBg = Color.decode("#F1F5F9");
            for (int i = 0; i < rows.size(); i++) {
                Color rowBg = (i % 2 == 0) ? Color.WHITE : altBg;
                for (String val : rows.get(i)) {
                    PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val, dataFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(4f);
                    table.addCell(cell);
                }
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public static ResponseEntity<byte[]> csvResponse(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    public static ResponseEntity<byte[]> pdfResponse(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
