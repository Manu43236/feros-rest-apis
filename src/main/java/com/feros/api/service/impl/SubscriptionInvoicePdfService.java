package com.feros.api.service.impl;

import com.feros.api.entity.SubscriptionInvoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SubscriptionInvoicePdfService {

    private static final Color NAVY     = new Color(15, 33, 55);
    private static final Color LIGHT_BG = new Color(240, 244, 250);
    private static final Color BORDER   = new Color(200, 212, 228);
    private static final Color GRAY_TXT = new Color(90, 105, 120);
    private static final Color GREEN    = new Color(21, 128, 61);

    private static final Font F_COMPANY  = new Font(Font.HELVETICA, 16, Font.BOLD,  NAVY);
    private static final Font F_ADDR     = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_TXT);
    private static final Font F_INV_TYPE = new Font(Font.HELVETICA, 14, Font.BOLD,  Color.WHITE);
    private static final Font F_LABEL    = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_TXT);
    private static final Font F_VALUE    = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.BLACK);
    private static final Font F_BODY     = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.BLACK);
    private static final Font F_BOLD     = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.BLACK);
    private static final Font F_TH       = new Font(Font.HELVETICA,  8, Font.BOLD,  Color.WHITE);
    private static final Font F_TD       = new Font(Font.HELVETICA,  8, Font.NORMAL, Color.BLACK);
    private static final Font F_TD_BOLD  = new Font(Font.HELVETICA,  8, Font.BOLD,  Color.BLACK);
    private static final Font F_TOTAL_L  = new Font(Font.HELVETICA, 10, Font.BOLD,  NAVY);
    private static final Font F_GRAND    = new Font(Font.HELVETICA, 12, Font.BOLD,  Color.WHITE);
    private static final Font F_SMALL    = new Font(Font.HELVETICA,  7, Font.NORMAL, GRAY_TXT);
    private static final Font F_WORDS    = new Font(Font.HELVETICA,  8, Font.ITALIC, GRAY_TXT);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final String HSN_SAC = "998315";

    public byte[] generate(SubscriptionInvoice inv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var tenant     = inv.getTenant();
            boolean isPro  = "PROFORMA".equals(inv.getInvoiceStatus());
            boolean isIGST = "INTER_STATE".equals(inv.getGstType());
            String docLabel = isPro ? "PROFORMA INVOICE" : "TAX INVOICE";
            String docNo    = isPro ? inv.getProformaNumber() : inv.getInvoiceNumber();
            LocalDate docDate = isPro
                    ? (inv.getPaymentDate() != null ? inv.getPaymentDate()
                       : inv.getCreatedAt() != null ? inv.getCreatedAt().toLocalDate() : LocalDate.now())
                    : (inv.getPaymentDate() != null ? inv.getPaymentDate() : LocalDate.now());
            LocalDate validUntil = docDate.plusDays(15);

            // ── 1. HEADER: Company info (left) | Invoice type banner (right) ──────
            PdfPTable header = new PdfPTable(new float[]{1.4f, 1f});
            header.setWidthPercentage(100);

            // Left: supplier
            PdfPCell leftCell = noBorder();
            leftCell.setPaddingBottom(6);
            leftCell.addElement(new Phrase("MandM Technologies", F_COMPANY));
            leftCell.addElement(new Phrase("2nd Floor, Dwaraka Meadows, Madhuravada, Visakhapatnam, Andhra Pradesh", F_ADDR));
            leftCell.addElement(new Phrase("PAN: ACHFM8981H", F_ADDR));
            leftCell.addElement(new Phrase("GSTIN/UIN: 37CHFM8981H1ZK", F_ADDR));
            leftCell.addElement(new Phrase("State: Andhra Pradesh  |  Registration in progress", F_ADDR));
            header.addCell(leftCell);

            // Right: coloured banner
            PdfPCell rightCell = noBorder();
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell(new Phrase(docLabel, F_INV_TYPE));
            bannerCell.setBackgroundColor(NAVY);
            bannerCell.setPadding(12);
            bannerCell.setBorder(Rectangle.NO_BORDER);
            bannerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            banner.addCell(bannerCell);
            rightCell.addElement(banner);
            header.addCell(rightCell);
            doc.add(header);

            // ── 2. META STRIP: Ref No, Dated, Valid Until, Period ─────────────────
            PdfPTable meta = new PdfPTable(new float[]{1, 1, 1, 1, 1});
            meta.setWidthPercentage(100);
            metaCell(meta, "Ref. No.",    docNo != null ? docNo : "—");
            metaCell(meta, "Dated",       docDate.format(DATE_FMT));
            metaCell(meta, isPro ? "Valid Until" : "Invoice Date",
                    isPro ? validUntil.format(DATE_FMT) : docDate.format(DATE_FMT));
            String periodFrom = inv.getPeriodStart() != null ? inv.getPeriodStart().format(DATE_FMT) : "—";
            String periodTo   = inv.getPeriodEnd()   != null ? inv.getPeriodEnd().format(DATE_FMT)   : "—";
            metaCell(meta, "Period From", periodFrom);
            metaCell(meta, "Period To",   periodTo);
            doc.add(meta);

            doc.add(gap(4));

            // ── 3. BILL TO ────────────────────────────────────────────────────────
            PdfPTable billTo = new PdfPTable(1);
            billTo.setWidthPercentage(100);
            PdfPCell btCell = new PdfPCell();
            btCell.setBorder(Rectangle.BOX);
            btCell.setBorderColor(BORDER);
            btCell.setPadding(7);

            Paragraph btLabel = new Paragraph("Bill To", F_LABEL);
            btLabel.setSpacingAfter(2);
            btCell.addElement(btLabel);

            String company = tenant.getCompanyName() != null ? tenant.getCompanyName().toUpperCase() : "—";
            btCell.addElement(new Phrase(company, F_VALUE));

            StringBuilder addr = new StringBuilder();
            if (tenant.getAddress() != null) addr.append(tenant.getAddress());
            if (tenant.getCity()    != null) addr.append(", ").append(tenant.getCity());
            if (tenant.getState()   != null) addr.append(", ").append(tenant.getState());
            if (tenant.getPincode() != null) addr.append(" - ").append(tenant.getPincode());
            if (!addr.isEmpty()) btCell.addElement(new Phrase(addr.toString(), F_ADDR));
            if (tenant.getGstin() != null) {
                btCell.addElement(new Phrase("GSTIN: " + tenant.getGstin()
                        + (tenant.getState() != null ? "  |  State: " + tenant.getState() : ""), F_ADDR));
            }
            billTo.addCell(btCell);
            doc.add(billTo);

            doc.add(gap(6));

            // ── 4. LINE ITEMS TABLE ───────────────────────────────────────────────
            PdfPTable items = new PdfPTable(new float[]{0.4f, 3f, 0.8f, 0.7f, 1f, 1.2f, 1.1f});
            items.setWidthPercentage(100);
            for (String h : new String[]{"#", "Description of Service", "HSN/SAC", "Vehicles", "Rate / Vehicle", "Duration", "Amount (Rs.)"}) {
                PdfPCell th = new PdfPCell(new Phrase(h, F_TH));
                th.setBackgroundColor(NAVY);
                th.setPadding(6);
                th.setBorder(Rectangle.NO_BORDER);
                if (h.equals("Amount (Rs.)") || h.equals("Vehicles") || h.equals("Rate / Vehicle")) {
                    th.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                items.addCell(th);
            }

            long periodDays = 0;
            if (inv.getPeriodStart() != null && inv.getPeriodEnd() != null) {
                periodDays = java.time.temporal.ChronoUnit.DAYS.between(inv.getPeriodStart(), inv.getPeriodEnd()) + 1;
            }
            double months = periodDays > 0 ? Math.round((periodDays / 30.0) * 10.0) / 10.0 : 0;
            String durationStr = months > 0 ? (months == Math.floor(months) ? (int) months + " months" : months + " months") : "—";

            BigDecimal vehicleBase = BigDecimal.ZERO;
            if (inv.getPricePerVehicle() != null && inv.getVehicleCount() != null && periodDays > 0) {
                vehicleBase = inv.getPricePerVehicle()
                        .multiply(new BigDecimal(inv.getVehicleCount()))
                        .multiply(new BigDecimal(periodDays).divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            int rowNum = 1;
            addItemRow(items, rowNum++,
                    "FEROS Platform Subscription - PREMIUM",
                    HSN_SAC,
                    inv.getVehicleCount() != null ? String.valueOf(inv.getVehicleCount()) : "—",
                    inv.getPricePerVehicle() != null ? fmt(inv.getPricePerVehicle()) : "—",
                    durationStr,
                    vehicleBase);

            // Additional charges
            if (inv.getAdditionalChargesJson() != null && !inv.getAdditionalChargesJson().isBlank()) {
                try {
                    String json = inv.getAdditionalChargesJson().trim().replaceAll("^\\[|]$", "");
                    for (String entry : json.split("\\},\\s*\\{")) {
                        entry = entry.replaceAll("[\\[\\]{}]", "");
                        String name   = entry.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1");
                        String amtStr = entry.replaceAll(".*\"amount\":([0-9.]+).*", "$1");
                        BigDecimal amt = new BigDecimal(amtStr).setScale(2, RoundingMode.HALF_UP);
                        addItemRow(items, rowNum++, name, HSN_SAC, "—", "—", "One-time setup fee", amt);
                    }
                } catch (Exception ignored) {}
            }

            doc.add(items);

            // ── 5. GST NOTE + SUBTOTAL ROW ────────────────────────────────────────
            BigDecimal base  = inv.getAmount()      != null ? inv.getAmount()      : BigDecimal.ZERO;
            BigDecimal gst   = inv.getGstAmount()   != null ? inv.getGstAmount()   : BigDecimal.ZERO;
            BigDecimal grand = inv.getTotalAmount()  != null ? inv.getTotalAmount() : BigDecimal.ZERO;

            PdfPTable subtotalRow = new PdfPTable(new float[]{3f, 1f});
            subtotalRow.setWidthPercentage(100);
            PdfPCell gstNote = new PdfPCell(new Phrase(
                    isPro ? "* GST @ 18% extra as applicable" : "", F_SMALL));
            gstNote.setBorder(Rectangle.TOP);
            gstNote.setBorderColor(BORDER);
            gstNote.setPadding(5);
            subtotalRow.addCell(gstNote);
            PdfPCell totalAmt = new PdfPCell(new Phrase("Total   Rs." + fmt(base), F_BOLD));
            totalAmt.setBorder(Rectangle.TOP);
            totalAmt.setBorderColor(BORDER);
            totalAmt.setPadding(5);
            totalAmt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalRow.addCell(totalAmt);
            doc.add(subtotalRow);

            // For tax invoice: show GST breakdown
            if (!isPro) {
                PdfPTable gstBreakdown = new PdfPTable(new float[]{3f, 1f});
                gstBreakdown.setWidthPercentage(100);
                if (isIGST) {
                    addSummaryRow(gstBreakdown, "IGST @ 18%", gst);
                } else {
                    BigDecimal half = gst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                    addSummaryRow(gstBreakdown, "CGST @ 9%", half);
                    addSummaryRow(gstBreakdown, "SGST @ 9%", gst.subtract(half));
                }
                doc.add(gstBreakdown);
            }

            doc.add(gap(4));

            // ── 6. AMOUNT IN WORDS + GRAND TOTAL ──────────────────────────────────
            PdfPTable grandRow = new PdfPTable(new float[]{1.8f, 1f});
            grandRow.setWidthPercentage(100);

            PdfPCell wordsCell = new PdfPCell();
            wordsCell.setBorder(Rectangle.NO_BORDER);
            wordsCell.setPadding(6);
            wordsCell.addElement(new Phrase("Amount in words:", F_LABEL));
            wordsCell.addElement(new Phrase("INR " + amountInWords(grand) + " Only", F_WORDS));
            grandRow.addCell(wordsCell);

            PdfPCell grandCell = new PdfPCell(new Phrase("Total Amount\nRs." + fmt(grand), F_GRAND));
            grandCell.setBackgroundColor(NAVY);
            grandCell.setBorder(Rectangle.NO_BORDER);
            grandCell.setPadding(10);
            grandCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            grandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            grandRow.addCell(grandCell);
            doc.add(grandRow);

            doc.add(gap(8));

            // ── 7. PAYMENT TERMS ──────────────────────────────────────────────────
            if (isPro) {
                Paragraph terms = new Paragraph(
                        "MandM Technologies is available for quotation and advance payment purpose only. "
                        + "It is not a Tax Invoice and is subject to change.\n"
                        + "Payment Terms: 100% advance on acceptance. Quotation valid within 15 days only.",
                        F_SMALL);
                doc.add(terms);
                doc.add(gap(6));
            }

            // ── 8. BANK DETAILS + AUTHORISED SIGNATORY ────────────────────────────
            PdfPTable footer = new PdfPTable(new float[]{1, 1});
            footer.setWidthPercentage(100);

            PdfPCell bankCell = noBorder();
            bankCell.addElement(new Phrase("Bank Details", F_BOLD));
            bankCell.addElement(new Phrase("MandM Technologies", F_BODY));
            bankCell.addElement(new Phrase("Current Account: HDFC Bank Ltd, Anakapalle Branch", F_ADDR));
            footer.addCell(bankCell);

            PdfPCell sigCell = noBorder();
            Paragraph sigFor = new Paragraph("For MandM Technologies", F_BODY);
            sigFor.setAlignment(Element.ALIGN_RIGHT);
            sigCell.addElement(sigFor);
            sigCell.addElement(gap(20));
            Paragraph sigLabel = new Paragraph("Authorised Signatory", F_LABEL);
            sigLabel.setAlignment(Element.ALIGN_RIGHT);
            sigCell.addElement(sigLabel);
            footer.addCell(sigCell);
            doc.add(footer);

            doc.add(gap(8));
            Paragraph generated = new Paragraph(
                    "This is a Computer Generated " + docLabel + " — not valid for Input Tax Credit",
                    F_SMALL);
            generated.setAlignment(Element.ALIGN_CENTER);
            doc.add(generated);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate subscription invoice PDF", e);
        }
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    private void metaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(6);
        cell.addElement(new Phrase(label, F_LABEL));
        cell.addElement(new Phrase(value != null ? value : "—", F_VALUE));
        table.addCell(cell);
    }

    private void addItemRow(PdfPTable table, int num, String desc, String hsn,
                            String vehicles, String rate, String duration, BigDecimal amount) {
        tdC(table, String.valueOf(num), F_TD, Element.ALIGN_CENTER);
        td(table, desc, F_TD);
        tdC(table, hsn, F_TD, Element.ALIGN_CENTER);
        tdC(table, vehicles, F_TD, Element.ALIGN_RIGHT);
        tdC(table, rate, F_TD, Element.ALIGN_RIGHT);
        td(table, duration, F_TD);
        tdC(table, fmt(amount), F_TD_BOLD, Element.ALIGN_RIGHT);
    }

    private void td(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setPadding(6); cell.setBorderColor(BORDER);
        table.addCell(cell);
    }

    private void tdC(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setPadding(6); cell.setBorderColor(BORDER);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, BigDecimal amount) {
        PdfPCell lc = new PdfPCell(new Phrase("", F_TD));
        lc.setBorder(Rectangle.NO_BORDER); lc.setPadding(3);
        table.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(label + "   Rs." + fmt(amount), F_TD));
        vc.setBorder(Rectangle.NO_BORDER); vc.setPadding(3);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(vc);
    }

    private PdfPCell noBorder() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4);
        return c;
    }

    private Paragraph gap(float pt) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(pt);
        p.setSpacingAfter(0);
        return p;
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "0.00";
        long paise = v.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).longValue();
        boolean neg = paise < 0;
        paise = Math.abs(paise);
        String dec = String.format("%02d", paise % 100);
        long rupees = paise / 100;
        if (rupees == 0) return (neg ? "-" : "") + "0." + dec;
        // Indian system: last 3 digits, then prepend groups of 2 with comma
        StringBuilder sb = new StringBuilder(String.format("%03d", rupees % 1000));
        rupees /= 1000;
        while (rupees > 0) {
            sb.insert(0, String.format("%02d,", rupees % 100));
            rupees /= 100;
        }
        // Strip any leading zero from the most-significant group
        String result = sb.toString().replaceFirst("^0", "");
        return (neg ? "-" : "") + result + "." + dec;
    }

    // ── Amount in words (Indian system) ──────────────────────────────────────

    private static final String[] ONES = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
            "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] TENS = {"", "", "Twenty", "Thirty", "Forty", "Fifty",
            "Sixty", "Seventy", "Eighty", "Ninety"};

    private String amountInWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Zero";
        long paise  = amount.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).longValue();
        long rupees = paise / 100;
        int  paiseR = (int)(paise % 100);
        StringBuilder sb = new StringBuilder();
        if (rupees >= 10000000) { sb.append(inWords((int)(rupees / 10000000))).append(" Crore "); rupees %= 10000000; }
        if (rupees >= 100000)   { sb.append(inWords((int)(rupees / 100000))).append(" Lakh ");   rupees %= 100000; }
        if (rupees >= 1000)     { sb.append(inWords((int)(rupees / 1000))).append(" Thousand "); rupees %= 1000; }
        if (rupees >= 100)      { sb.append(inWords((int)(rupees / 100))).append(" Hundred ");   rupees %= 100; }
        if (rupees > 0)         { sb.append(inWords((int) rupees)).append(" "); }
        sb.append("Rupees");
        if (paiseR > 0)         { sb.append(" and ").append(inWords(paiseR)).append(" Paise"); }
        return sb.toString().trim();
    }

    private String inWords(int n) {
        if (n < 20) return ONES[n];
        return TENS[n / 10] + (n % 10 != 0 ? " " + ONES[n % 10] : "");
    }
}
