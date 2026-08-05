package com.feros.api.service.impl;

import com.feros.api.entity.SubscriptionInvoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class SubscriptionInvoicePdfService {

    private static final Color NAVY    = new Color(15, 33, 55);
    private static final Color GRAY    = new Color(90, 105, 120);
    private static final Color SUCCESS = new Color(21, 128, 61);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
    private static final Font FONT_TITLE  = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);
    private static final Font FONT_BODY   = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font FONT_BOLD   = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font FONT_GRAY   = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY);
    private static final Font FONT_TOTAL  = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);

    public byte[] generate(SubscriptionInvoice inv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var tenant = inv.getTenant();
            String company = tenant.getCompanyName() != null ? tenant.getCompanyName().toUpperCase() : "FEROS";
            boolean isProforma = "PROFORMA".equals(inv.getInvoiceStatus());
            boolean isInterState = "INTER_STATE".equals(inv.getGstType());
            String docLabel = isProforma ? "Proforma Invoice" : "Tax Invoice";
            String docNumber = isProforma ? inv.getProformaNumber() : inv.getInvoiceNumber();

            // ── Header banner ─────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(NAVY);
            hCell.setPadding(14);
            hCell.setBorder(Rectangle.NO_BORDER);
            hCell.addElement(new Phrase(company, FONT_HEADER));
            hCell.addElement(new Phrase(docLabel, new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(180, 200, 230))));
            header.addCell(hCell);
            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // ── Supplier block (MandM Technologies) ───────────────────────────
            doc.add(new Paragraph("Supplier: M&M Technologies", FONT_BOLD));
            doc.add(new Paragraph("Andhra Pradesh, India", FONT_GRAY));
            doc.add(new Paragraph("GSTIN: 37CHFM8981H1ZK", FONT_GRAY));
            doc.add(Chunk.NEWLINE);

            // ── Billed to (Tenant) ─────────────────────────────────────────────
            doc.add(new Paragraph("Billed To:", FONT_BOLD));
            doc.add(new Paragraph(company, FONT_BODY));
            if (tenant.getAddress() != null || tenant.getCity() != null) {
                StringBuilder addr = new StringBuilder();
                if (tenant.getAddress() != null) addr.append(tenant.getAddress());
                if (tenant.getCity() != null) addr.append(", ").append(tenant.getCity());
                if (tenant.getState() != null) addr.append(", ").append(tenant.getState());
                if (tenant.getPincode() != null) addr.append(" - ").append(tenant.getPincode());
                doc.add(new Paragraph(addr.toString(), FONT_GRAY));
            }
            if (tenant.getGstin() != null) {
                doc.add(new Paragraph("GSTIN: " + tenant.getGstin(), FONT_GRAY));
            }
            doc.add(Chunk.NEWLINE);

            // ── Invoice meta ──────────────────────────────────────────────────
            PdfPTable meta = new PdfPTable(new float[]{1, 1});
            meta.setWidthPercentage(100);
            addMetaCell(meta, isProforma ? "Proforma No" : "Invoice No", docNumber != null ? docNumber : "—");
            String dateLabel = isProforma ? "Proforma Date" : "Invoice Date";
            String dateValue = isProforma
                    ? (inv.getCreatedAt() != null ? inv.getCreatedAt().format(DATE_FMT) : "—")
                    : (inv.getPaymentDate() != null ? inv.getPaymentDate().format(DATE_FMT) : "—");
            addMetaCell(meta, dateLabel, dateValue);
            addMetaCell(meta, "Plan", inv.getPlanName() != null ? inv.getPlanName() : "—");
            addMetaCell(meta, "Billing Cycle", formatCycle(inv.getBillingCycle()));
            addMetaCell(meta, "Vehicle Count",
                    inv.getVehicleCount() != null ? inv.getVehicleCount() + " vehicles" : "—");
            addMetaCell(meta, "Price / Vehicle / Month",
                    inv.getPricePerVehicle() != null ? "₹" + inv.getPricePerVehicle().setScale(2, RoundingMode.HALF_UP) : "—");
            addMetaCell(meta, "Period Start",
                    inv.getPeriodStart() != null ? inv.getPeriodStart().format(DATE_FMT) : "—");
            addMetaCell(meta, "Period End",
                    inv.getPeriodEnd() != null ? inv.getPeriodEnd().format(DATE_FMT) : "—");
            if (!isProforma && inv.getPaymentMode() != null) {
                addMetaCell(meta, "Payment Mode", inv.getPaymentMode());
            }
            if (inv.getPaymentRef() != null) {
                addMetaCell(meta, "Payment Ref", inv.getPaymentRef());
            }
            doc.add(meta);
            doc.add(Chunk.NEWLINE);

            // ── Amount summary ────────────────────────────────────────────────
            doc.add(sectionTitle(isProforma ? "ESTIMATED AMOUNT" : "AMOUNT SUMMARY"));
            PdfPTable totals = new PdfPTable(new float[]{3, 1.5f});
            totals.setWidthPercentage(60);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal gst = inv.getGstAmount() != null ? inv.getGstAmount() : BigDecimal.ZERO;
            addTotalRow(totals, "Taxable Amount", inv.getAmount(), false);
            if (isInterState) {
                addTotalRow(totals, "IGST (18%)", gst, false);
            } else {
                BigDecimal half = gst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                addTotalRow(totals, "CGST (9%)", half, false);
                addTotalRow(totals, "SGST (9%)", gst.subtract(half), false);
            }
            addTotalRow(totals, isProforma ? "EXPECTED TOTAL" : "TOTAL", inv.getTotalAmount(), true);
            doc.add(totals);

            // ── Proforma note ─────────────────────────────────────────────────
            if (isProforma) {
                doc.add(Chunk.NEWLINE);
                Paragraph note = new Paragraph(
                        "This is a proforma invoice only. It is not a tax document. "
                        + "A GST Tax Invoice will be issued upon receipt of payment.", FONT_GRAY);
                note.setAlignment(Element.ALIGN_CENTER);
                doc.add(note);
            }

            // ── Footer ────────────────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph(
                    "System-generated by FEROS Fleet Management.", FONT_GRAY);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate subscription invoice PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(220, 228, 240));
        cell.setPadding(6);
        cell.addElement(new Phrase(label, FONT_GRAY));
        cell.addElement(new Phrase(value != null ? value : "—", FONT_BOLD));
        table.addCell(cell);
    }

    private Paragraph sectionTitle(String title) {
        Paragraph p = new Paragraph(title, FONT_TITLE);
        p.setSpacingBefore(4);
        p.setSpacingAfter(6);
        return p;
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal amount, boolean highlight) {
        Font lf = highlight ? FONT_TOTAL : FONT_BODY;
        Font vf = highlight ? new Font(Font.HELVETICA, 11, Font.BOLD, SUCCESS) : FONT_BODY;
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setPadding(5);
        lc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        lc.setBorderColor(new Color(220, 228, 240));
        PdfPCell vc = new PdfPCell(new Phrase(
                "₹" + (amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : "0.00"), vf));
        vc.setPadding(5);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        vc.setBorderColor(new Color(220, 228, 240));
        table.addCell(lc);
        table.addCell(vc);
    }

    private String formatCycle(String cycle) {
        if (cycle == null) return "—";
        return switch (cycle) {
            case "MONTHLY" -> "Monthly";
            case "THREE_MONTHS" -> "3 Months";
            case "SIX_MONTHS" -> "6 Months";
            case "YEARLY" -> "Annual";
            default -> cycle;
        };
    }
}
