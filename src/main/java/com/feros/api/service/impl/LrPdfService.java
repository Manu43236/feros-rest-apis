package com.feros.api.service.impl;

import com.feros.api.entity.*;
import com.feros.api.repository.LrChargeRepository;
import com.feros.api.repository.LrCheckpostRepository;
import com.feros.api.repository.LrRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LrPdfService {

    private final LrRepository lrRepository;
    private final LrCheckpostRepository lrCheckpostRepository;
    private final LrChargeRepository lrChargeRepository;

    private static final Color NAVY       = new Color(30, 58, 95);
    private static final Color LIGHT_BLUE = new Color(239, 246, 255);
    private static final Color GRAY       = new Color(100, 116, 139);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] generatePdf(Long lrId) {
        Lr lr = lrRepository.findById(lrId)
                .orElseThrow(() -> new RuntimeException("LR not found"));
        List<LrCheckpost> checkposts = lrCheckpostRepository.findByLrIdAndIsActiveTrue(lrId);
        List<LrCharge>    charges    = lrChargeRepository.findByLrIdAndIsActiveTrue(lrId);

        Tenant tenant  = lr.getTenant();
        Order  order   = lr.getOrder();
        Vehicle vehicle = lr.getVehicleAllocation().getVehicle();

        Font navyBold16 = new Font(Font.HELVETICA, 15, Font.BOLD,   NAVY);
        Font bold8      = new Font(Font.HELVETICA,  7, Font.BOLD,   Color.BLACK);
        Font bold8Navy  = new Font(Font.HELVETICA,  7, Font.BOLD,   NAVY);
        Font reg7       = new Font(Font.HELVETICA,  7, Font.NORMAL, Color.BLACK);
        Font gray6      = new Font(Font.HELVETICA,  6, Font.NORMAL, GRAY);
        Font small6     = new Font(Font.HELVETICA,  6, Font.NORMAL, GRAY);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A5, 24, 24, 24, 24);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── PAN / GSTIN row ───────────────────────────────────────────
            if (tenant.getPanNumber() != null || tenant.getGstin() != null) {
                PdfPTable meta = new PdfPTable(2);
                meta.setWidthPercentage(100);
                noBorderCell(meta, "PAN : " + blank(tenant.getPanNumber()),  Element.ALIGN_LEFT,  small6);
                noBorderCell(meta, "GSTIN : " + blank(tenant.getGstin()),    Element.ALIGN_RIGHT, small6);
                doc.add(meta);
            }

            // ── Company name ──────────────────────────────────────────────
            Paragraph name = new Paragraph(tenant.getCompanyName(), navyBold16);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingBefore(2);
            name.setSpacingAfter(2);
            doc.add(name);

            if (tenant.getAddress() != null) {
                Paragraph addr = new Paragraph(tenant.getAddress(), gray6);
                addr.setAlignment(Element.ALIGN_CENTER);
                addr.setSpacingAfter(3);
                doc.add(addr);
            }

            // ── LORRY RECEIPT title + divider ─────────────────────────────
            Paragraph title = new Paragraph("LORRY RECEIPT", bold8);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(3);
            doc.add(title);

            PdfPTable divLine = new PdfPTable(1);
            divLine.setWidthPercentage(100);
            divLine.setSpacingAfter(4);
            PdfPCell dl = new PdfPCell(new Phrase(" "));
            dl.setBorderWidthBottom(1.2f); dl.setBorderColorBottom(NAVY);
            dl.setBorderWidthTop(0); dl.setBorderWidthLeft(0); dl.setBorderWidthRight(0);
            dl.setFixedHeight(2);
            divLine.addCell(dl);
            doc.add(divLine);

            // ── INFO SECTION ──────────────────────────────────────────────
            String fromCity = order.getSourceCity()      != null ? order.getSourceCity().getName()      : "";
            String toCity   = order.getDestinationCity() != null ? order.getDestinationCity().getName() : "";

            PdfPTable info = new PdfPTable(new float[]{2.2f, 1f});
            info.setWidthPercentage(100);

            // Left: consignor / consignee / from-to
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.BOX); left.setBorderColor(NAVY); left.setPadding(5);

            left.addElement(new Paragraph("Consignor's Name & Address", bold8));
            Paragraph consignorVal = new Paragraph(fromCity.isEmpty() ? "—" : fromCity, bold8Navy);
            consignorVal.setSpacingBefore(6);
            left.addElement(consignorVal);
            if (order.getSourceAddress() != null)
                left.addElement(new Paragraph(order.getSourceAddress(), gray6));

            Paragraph consigneeLabel = new Paragraph("Consignee Name & Address", bold8);
            consigneeLabel.setSpacingBefore(8);
            left.addElement(consigneeLabel);
            Paragraph consigneeVal = new Paragraph(order.getClient().getClientName(), bold8Navy);
            consigneeVal.setSpacingBefore(6);
            left.addElement(consigneeVal);
            if (!toCity.isEmpty()) left.addElement(new Paragraph(toCity, gray6));
            if (order.getDestinationAddress() != null)
                left.addElement(new Paragraph(order.getDestinationAddress(), gray6));

            Paragraph fromTo = new Paragraph();
            fromTo.setSpacingBefore(8);
            fromTo.add(new Chunk("From  ", bold8));
            fromTo.add(new Chunk((fromCity.isEmpty() ? "—" : fromCity) + "   →   " + (toCity.isEmpty() ? "—" : toCity), bold8Navy));
            left.addElement(fromTo);
            info.addCell(left);

            // Right: LR No / Date / Vehicle
            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.BOX); right.setBorderColor(NAVY); right.setPadding(0);

            PdfPTable rightInner = new PdfPTable(1);
            rightInner.setWidthPercentage(100);

            PdfPCell lrNoRow = new PdfPCell(new Phrase("No. : " + lr.getLrNumber(), bold8));
            lrNoRow.setBorderWidthBottom(0.5f); lrNoRow.setBorderColorBottom(NAVY);
            lrNoRow.setBorderWidthTop(0); lrNoRow.setBorderWidthLeft(0); lrNoRow.setBorderWidthRight(0);
            lrNoRow.setPadding(4); lrNoRow.setMinimumHeight(18);
            rightInner.addCell(lrNoRow);

            String lrDateStr = lr.getLrDate() != null ? lr.getLrDate().format(DATE_FMT) : "";
            PdfPCell dateRow = new PdfPCell(new Phrase("Date : " + lrDateStr, bold8));
            dateRow.setBorderWidthBottom(0.5f); dateRow.setBorderColorBottom(NAVY);
            dateRow.setBorderWidthTop(0); dateRow.setBorderWidthLeft(0); dateRow.setBorderWidthRight(0);
            dateRow.setPadding(4); dateRow.setMinimumHeight(18);
            rightInner.addCell(dateRow);

            PdfPCell vehCell = new PdfPCell();
            vehCell.setBorderWidthTop(1f); vehCell.setBorderColorTop(NAVY);
            vehCell.setBorderWidthBottom(0); vehCell.setBorderWidthLeft(0); vehCell.setBorderWidthRight(0);
            vehCell.setPadding(4);
            vehCell.addElement(new Paragraph("Vehicle No.", bold8));
            vehCell.addElement(new Paragraph(vehicle.getRegistrationNumber(),
                    new Font(Font.HELVETICA, 9, Font.BOLD, NAVY)));
            if (vehicle.getVehicleType() != null)
                vehCell.addElement(new Paragraph(vehicle.getVehicleType().getName(), gray6));
            rightInner.addCell(vehCell);

            right.addElement(rightInner);
            info.addCell(right);
            doc.add(info);

            // ── MAIN TABLE ────────────────────────────────────────────────
            PdfPTable table = new PdfPTable(new float[]{0.6f, 2.1f, 0.8f, 0.8f, 1.3f, 0.8f, 0.5f, 1.1f});
            table.setWidthPercentage(100);

            // Header row 1
            th(table, "No. of\nPackage"); th(table, "Material");
            PdfPCell wtH = span2Header("Weight"); table.addCell(wtH);
            th(table, "Rate");
            PdfPCell frH = span2Header("Freight"); table.addCell(frH);
            th(table, "Remarks");

            // Header row 2
            th(table, " "); th(table, " ");
            th(table, "Actual"); th(table, "Charged");
            th(table, " ");
            th(table, "Rs."); th(table, "Ps.");
            th(table, " ");

            // Freight calculation
            BigDecimal billedWeight = "LOADED_WEIGHT".equals(order.getBillingOn().name())
                    ? zero(lr.getLoadedWeight())
                    : zero(lr.getDeliveredWeight() != null ? lr.getDeliveredWeight() : lr.getLoadedWeight());

            BigDecimal freightTotal = "PER_TON".equals(order.getFreightRateType().name())
                    ? order.getFreightRate().multiply(billedWeight)
                    : order.getFreightRate();

            int fRs = freightTotal.intValue();
            int fPs = freightTotal.subtract(BigDecimal.valueOf(fRs))
                    .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();

            String rateText = "Rs. " + order.getFreightRate().toPlainString()
                    + " / " + order.getFreightRateType().name().replace("_", " ");
            String material = order.getMaterialType() != null ? order.getMaterialType().getName() : "—";

            // Data row
            td(table, "1", Element.ALIGN_CENTER, reg7);
            tdLeft(table, material, bold8);
            td(table, lr.getLoadedWeight() != null ? lr.getLoadedWeight().toPlainString() + " T" : "—", Element.ALIGN_CENTER, reg7);
            td(table, billedWeight.compareTo(BigDecimal.ZERO) > 0 ? billedWeight.toPlainString() + " T" : "—", Element.ALIGN_CENTER, reg7);
            td(table, rateText, Element.ALIGN_CENTER, new Font(Font.HELVETICA, 6, Font.NORMAL, Color.BLACK));
            td(table, fRs > 0 ? String.format("%,d", fRs) : "—", Element.ALIGN_CENTER, bold8);
            td(table, String.format("%02d", fPs), Element.ALIGN_CENTER, reg7);
            tdLeft(table, blank(lr.getRemarks()), reg7);

            // Extra charges row
            BigDecimal totalCharges = BigDecimal.ZERO;
            if (!charges.isEmpty()) {
                totalCharges = charges.stream().map(LrCharge::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                String chargeNames = charges.stream()
                        .map(ch -> ch.getChargeType() != null ? ch.getChargeType().getName() : "Charge")
                        .distinct().reduce((a, b) -> a + ", " + b).orElse("Extra Charges");
                td(table, " ", Element.ALIGN_CENTER, reg7);
                tdLeft(table, chargeNames, gray6);
                td(table, " ", Element.ALIGN_CENTER, reg7); td(table, " ", Element.ALIGN_CENTER, reg7);
                td(table, "Extra Charges", Element.ALIGN_CENTER, gray6);
                td(table, String.format("%,d", totalCharges.intValue()), Element.ALIGN_CENTER, bold8);
                td(table, "00", Element.ALIGN_CENTER, reg7);
                tdLeft(table, " ", reg7);
            }

            // Checkpost fines row
            BigDecimal totalFines = checkposts.stream()
                    .filter(cp -> cp.getFineAmount() != null)
                    .map(LrCheckpost::getFineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalFines.compareTo(BigDecimal.ZERO) > 0) {
                String cpText = "Checkpost Fines (" + checkposts.size() + ")";
                td(table, " ", Element.ALIGN_CENTER, reg7);
                tdLeft(table, cpText, gray6);
                td(table, " ", Element.ALIGN_CENTER, reg7); td(table, " ", Element.ALIGN_CENTER, reg7);
                td(table, "Fines", Element.ALIGN_CENTER, gray6);
                td(table, String.format("%,d", totalFines.intValue()), Element.ALIGN_CENTER, bold8);
                td(table, "00", Element.ALIGN_CENTER, reg7);
                tdLeft(table, " ", reg7);
            }

            // Total row
            BigDecimal grandTotal = freightTotal.add(totalCharges).add(totalFines);
            int gRs = grandTotal.intValue();
            int gPs = grandTotal.subtract(BigDecimal.valueOf(gRs))
                    .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();

            PdfPCell totalLabel = new PdfPCell(new Phrase("Total Freight", bold8));
            totalLabel.setColspan(5); totalLabel.setBackgroundColor(LIGHT_BLUE);
            totalLabel.setBorderColor(NAVY); totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setPadding(3);
            table.addCell(totalLabel);

            PdfPCell gRsCell = new PdfPCell(new Phrase(String.format("%,d", gRs),
                    new Font(Font.HELVETICA, 7, Font.BOLD, NAVY)));
            gRsCell.setBackgroundColor(LIGHT_BLUE); gRsCell.setBorderColor(NAVY);
            gRsCell.setHorizontalAlignment(Element.ALIGN_CENTER); gRsCell.setPadding(3);
            table.addCell(gRsCell);

            PdfPCell gPsCell = new PdfPCell(new Phrase(String.format("%02d", gPs), reg7));
            gPsCell.setBackgroundColor(LIGHT_BLUE); gPsCell.setBorderColor(NAVY);
            gPsCell.setHorizontalAlignment(Element.ALIGN_CENTER); gPsCell.setPadding(3);
            table.addCell(gPsCell);
            tdLeft(table, " ", reg7);

            doc.add(table);

            // ── FOOTER ───────────────────────────────────────────────────
            PdfPTable footer = new PdfPTable(new float[]{1.3f, 1f});
            footer.setWidthPercentage(100);

            PdfPCell footLeft = new PdfPCell();
            footLeft.setBorderWidthTop(0); footLeft.setBorderColor(NAVY); footLeft.setPadding(6);
            footLeft.addElement(new Paragraph("Owner M/s. " + tenant.getCompanyName(), bold8));
            Paragraph driverLine = new Paragraph("Driver: ________________   D/L.No.: ________________", bold8);
            driverLine.setSpacingBefore(8);
            footLeft.addElement(driverLine);
            Paragraph declaredLine = new Paragraph("Declared Value Rs.: ________________", bold8);
            declaredLine.setSpacingBefore(8);
            footLeft.addElement(declaredLine);
            footer.addCell(footLeft);

            PdfPCell footRight = new PdfPCell();
            footRight.setBorderWidthTop(0); footRight.setBorderColor(NAVY); footRight.setPadding(6);
            footRight.addElement(new Paragraph("For " + tenant.getCompanyName(), bold8));
            Paragraph sig = new Paragraph("\n\n\nSupervisor", bold8);
            sig.setAlignment(Element.ALIGN_RIGHT);
            footRight.addElement(sig);
            footer.addCell(footRight);

            doc.add(footer);

            // ── GENERATED BY ─────────────────────────────────────────────
            String genLine = "Generated by FEROS  |  " + lr.getCreatedBy().getName()
                    + (lr.getCreatedAt() != null ? "  |  " + lr.getCreatedAt().format(DATE_FMT) : "");
            Paragraph gen = new Paragraph(genLine, small6);
            gen.setAlignment(Element.ALIGN_RIGHT);
            gen.setSpacingBefore(4);
            doc.add(gen);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LR PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void noBorderCell(PdfPTable t, String text, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPadding(2);
        t.addCell(c);
    }

    private void th(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 6.5f, Font.BOLD, Color.BLACK)));
        c.setBackgroundColor(LIGHT_BLUE);
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(3);
        t.addCell(c);
    }

    private PdfPCell span2Header(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 6.5f, Font.BOLD, Color.BLACK)));
        c.setColspan(2);
        c.setBackgroundColor(LIGHT_BLUE);
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(3);
        return c;
    }

    private void td(PdfPTable t, String text, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(align);
        c.setPadding(3);
        c.setMinimumHeight(18);
        t.addCell(c);
    }

    private void tdLeft(PdfPTable t, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setPadding(3);
        c.setMinimumHeight(18);
        t.addCell(c);
    }

    private String blank(String s) { return s != null ? s : ""; }

    private BigDecimal zero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
}
