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

    private static final Color NAVY      = new Color(15, 33, 55);
    private static final Color LIGHT_BG  = new Color(240, 244, 250);
    private static final Color BORDER    = new Color(210, 220, 235);
    private static final Color GRAY_TXT  = new Color(90, 105, 120);
    private static final Color GREEN     = new Color(21, 128, 61);

    private static final Font F_CO_NAME  = new Font(Font.HELVETICA, 14, Font.BOLD,  NAVY);
    private static final Font F_DOC_TYPE = new Font(Font.HELVETICA, 10, Font.BOLD,  NAVY);
    private static final Font F_LABEL    = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_TXT);
    private static final Font F_VALUE    = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.BLACK);
    private static final Font F_BODY     = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.BLACK);
    private static final Font F_BOLD     = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.BLACK);
    private static final Font F_TH       = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.WHITE);
    private static final Font F_TOTAL    = new Font(Font.HELVETICA, 10, Font.BOLD,  NAVY);
    private static final Font F_GRAND    = new Font(Font.HELVETICA, 11, Font.BOLD,  GREEN);
    private static final Font F_SMALL    = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_TXT);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // SAC code for cloud/SaaS software services
    private static final String SAC_CODE = "998315";

    public byte[] generate(SubscriptionInvoice inv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var tenant = inv.getTenant();
            boolean isProforma   = "PROFORMA".equals(inv.getInvoiceStatus());
            boolean isInterState = "INTER_STATE".equals(inv.getGstType());
            String  docLabel     = isProforma ? "PROFORMA INVOICE" : "TAX INVOICE";
            String  docNumber    = isProforma ? inv.getProformaNumber() : inv.getInvoiceNumber();
            String  docDate      = isProforma
                    ? (inv.getCreatedAt()   != null ? inv.getCreatedAt().format(DATE_FMT)   : "—")
                    : (inv.getPaymentDate() != null ? inv.getPaymentDate().format(DATE_FMT) : "—");

            // ── TOP HEADER: Supplier (left) | Invoice meta (right) ─────────────
            PdfPTable topHeader = new PdfPTable(new float[]{1, 1});
            topHeader.setWidthPercentage(100);

            // Left — Supplier
            PdfPCell supplierCell = new PdfPCell();
            supplierCell.setBorder(Rectangle.BOX);
            supplierCell.setBorderColor(BORDER);
            supplierCell.setBackgroundColor(LIGHT_BG);
            supplierCell.setPadding(10);
            supplierCell.addElement(new Phrase("M&M Technologies", F_CO_NAME));
            supplierCell.addElement(spacer(3));
            supplierCell.addElement(labelVal("GSTIN", "37CHFM8981H1ZK"));
            supplierCell.addElement(labelVal("State", "Andhra Pradesh (State Code: 37)"));
            supplierCell.addElement(labelVal("Email", "manikanta.chadaram1992@gmail.com"));
            topHeader.addCell(supplierCell);

            // Right — Invoice meta
            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.BOX);
            metaCell.setBorderColor(BORDER);
            metaCell.setPadding(10);
            metaCell.addElement(new Phrase(docLabel, F_DOC_TYPE));
            metaCell.addElement(spacer(4));
            metaCell.addElement(labelVal(isProforma ? "Proforma No." : "Invoice No.", docNumber != null ? docNumber : "—"));
            metaCell.addElement(labelVal("Date", docDate));
            if (!isProforma && inv.getPaymentMode() != null) {
                metaCell.addElement(labelVal("Payment Mode", inv.getPaymentMode()));
            }
            if (inv.getPaymentRef() != null) {
                metaCell.addElement(labelVal("Payment Ref", inv.getPaymentRef()));
            }
            metaCell.addElement(labelVal("GST Type", isInterState ? "Inter-State (IGST)" : "Intra-State (CGST+SGST)"));
            topHeader.addCell(metaCell);

            doc.add(topHeader);

            // ── BILL TO ──────────────────────────────────────────────────────────
            PdfPTable billTo = new PdfPTable(1);
            billTo.setWidthPercentage(100);
            PdfPCell btCell = new PdfPCell();
            btCell.setBorder(Rectangle.BOX);
            btCell.setBorderColor(BORDER);
            btCell.setPadding(8);
            btCell.addElement(new Phrase("Bill To", F_LABEL));
            btCell.addElement(spacer(2));
            String company = tenant.getCompanyName() != null ? tenant.getCompanyName() : "—";
            btCell.addElement(new Phrase(company, F_VALUE));
            StringBuilder addr = new StringBuilder();
            if (tenant.getAddress() != null) addr.append(tenant.getAddress());
            if (tenant.getCity()    != null) addr.append(", ").append(tenant.getCity());
            if (tenant.getState()   != null) addr.append(", ").append(tenant.getState());
            if (tenant.getPincode() != null) addr.append(" - ").append(tenant.getPincode());
            if (!addr.isEmpty()) btCell.addElement(new Phrase(addr.toString(), F_BODY));
            if (tenant.getGstin() != null) btCell.addElement(labelVal("GSTIN", tenant.getGstin()));
            billTo.addCell(btCell);
            doc.add(billTo);

            // ── LINE ITEMS TABLE ─────────────────────────────────────────────────
            doc.add(spacer(6));
            PdfPTable items = new PdfPTable(new float[]{0.5f, 3.5f, 1f, 1f, 1f, 1.2f});
            items.setWidthPercentage(100);
            addTh(items, "#");
            addTh(items, "Description");
            addTh(items, "SAC");
            addTh(items, "Period");
            addTh(items, "Vehicles");
            addTh(items, "Taxable Amount");

            // Vehicle subscription row
            long periodDays = 0;
            if (inv.getPeriodStart() != null && inv.getPeriodEnd() != null) {
                periodDays = java.time.temporal.ChronoUnit.DAYS.between(inv.getPeriodStart(), inv.getPeriodEnd()) + 1;
            }
            String periodStr = (inv.getPeriodStart() != null ? inv.getPeriodStart().format(DATE_FMT) : "—")
                    + " to " + (inv.getPeriodEnd() != null ? inv.getPeriodEnd().format(DATE_FMT) : "—");
            BigDecimal vehicleBase = BigDecimal.ZERO;
            if (inv.getPricePerVehicle() != null && inv.getVehicleCount() != null && periodDays > 0) {
                vehicleBase = inv.getPricePerVehicle()
                        .multiply(new BigDecimal(inv.getVehicleCount()))
                        .multiply(new BigDecimal(periodDays).divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            int rowNum = 1;
            addItemRow(items, rowNum++, "Fleet Management SaaS Subscription",
                    SAC_CODE, periodStr,
                    inv.getVehicleCount() != null ? inv.getVehicleCount() + " @ ₹" + inv.getPricePerVehicle() + "/mo" : "—",
                    vehicleBase);

            // Additional charge rows
            BigDecimal extraTotal = BigDecimal.ZERO;
            if (inv.getAdditionalChargesJson() != null && !inv.getAdditionalChargesJson().isBlank()) {
                try {
                    String json = inv.getAdditionalChargesJson().trim().replaceAll("^\\[|]$", "");
                    for (String entry : json.split("\\},\\s*\\{")) {
                        entry = entry.replaceAll("[\\[\\]{}]", "");
                        String name   = entry.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1");
                        String amtStr = entry.replaceAll(".*\"amount\":([0-9.]+).*", "$1");
                        BigDecimal amt = new BigDecimal(amtStr);
                        extraTotal = extraTotal.add(amt);
                        addItemRow(items, rowNum++, name, SAC_CODE, "—", "—", amt);
                    }
                } catch (Exception ignored) {}
            }

            doc.add(items);

            // ── TOTALS SECTION ───────────────────────────────────────────────────
            doc.add(spacer(4));
            PdfPTable totals = new PdfPTable(new float[]{3, 1.5f});
            totals.setWidthPercentage(42);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal base = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
            BigDecimal gst  = inv.getGstAmount() != null ? inv.getGstAmount() : BigDecimal.ZERO;
            BigDecimal grandTotal = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;

            addTotalRow(totals, "Taxable Amount", base, false);
            if (isInterState) {
                addTotalRow(totals, "IGST @ 18%", gst, false);
            } else {
                BigDecimal half = gst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                addTotalRow(totals, "CGST @ 9%",  half, false);
                addTotalRow(totals, "SGST @ 9%",  gst.subtract(half), false);
            }
            addGrandTotalRow(totals, isProforma ? "ESTIMATED TOTAL" : "GRAND TOTAL", grandTotal);
            doc.add(totals);

            // ── NOTES ────────────────────────────────────────────────────────────
            doc.add(spacer(10));
            if (isProforma) {
                Paragraph note = new Paragraph(
                        "Note: This is a Proforma Invoice only. It is not a GST Tax Invoice. "
                        + "An official Tax Invoice will be issued upon receipt of payment.", F_SMALL);
                doc.add(note);
            } else {
                Paragraph note = new Paragraph(
                        "This is a computer-generated Tax Invoice. No signature required.", F_SMALL);
                doc.add(note);
            }

            // ── FOOTER ───────────────────────────────────────────────────────────
            doc.add(spacer(8));
            Paragraph footer = new Paragraph("Powered by FEROS Fleet Management  |  M&M Technologies, Andhra Pradesh", F_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate subscription invoice PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Phrase labelVal(String label, String value) {
        Phrase p = new Phrase();
        p.add(new Chunk(label + ": ", F_LABEL));
        p.add(new Chunk(value != null ? value : "—", F_VALUE));
        return p;
    }

    private Paragraph spacer(float pt) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(pt);
        p.setSpacingAfter(0);
        return p;
    }

    private void addTh(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_TH));
        cell.setBackgroundColor(NAVY);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addItemRow(PdfPTable table, int num, String desc, String sac, String period, String vehicles, BigDecimal amount) {
        Color bc = BORDER;
        PdfPCell c1 = td(String.valueOf(num), F_BODY); c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell c2 = td(desc, F_BODY);
        PdfPCell c3 = td(sac, F_BODY); c3.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell c4 = td(period, F_SMALL);
        PdfPCell c5 = td(vehicles, F_SMALL); c5.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell c6 = td("₹" + amount.setScale(2, RoundingMode.HALF_UP), F_BOLD);
        c6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell c : new PdfPCell[]{c1, c2, c3, c4, c5, c6}) {
            c.setBorderColor(bc);
        }
        table.addCell(c1); table.addCell(c2); table.addCell(c3);
        table.addCell(c4); table.addCell(c5); table.addCell(c6);
    }

    private PdfPCell td(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setPadding(6);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal amount, boolean bold) {
        Font lf = bold ? F_TOTAL : F_BODY;
        Font vf = bold ? F_TOTAL : F_BODY;
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setPadding(5); lc.setBorder(Rectangle.BOTTOM); lc.setBorderColor(BORDER);
        PdfPCell vc = new PdfPCell(new Phrase("₹" + (amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : "0.00"), vf));
        vc.setPadding(5); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(Rectangle.BOTTOM); vc.setBorderColor(BORDER);
        table.addCell(lc); table.addCell(vc);
    }

    private void addGrandTotalRow(PdfPTable table, String label, BigDecimal amount) {
        PdfPCell lc = new PdfPCell(new Phrase(label, F_TOTAL));
        lc.setBackgroundColor(LIGHT_BG);
        lc.setPadding(7); lc.setBorder(Rectangle.BOX); lc.setBorderColor(BORDER);
        PdfPCell vc = new PdfPCell(new Phrase("₹" + (amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : "0.00"), F_GRAND));
        vc.setBackgroundColor(LIGHT_BG);
        vc.setPadding(7); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(Rectangle.BOX); vc.setBorderColor(BORDER);
        table.addCell(lc); table.addCell(vc);
    }
}
