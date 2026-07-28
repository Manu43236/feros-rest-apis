package com.feros.api.service.impl;

import com.feros.api.entity.*;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.*;
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

    private final LrRepository                    lrRepository;
    private final LrCheckpostRepository           lrCheckpostRepository;
    private final LrChargeRepository              lrChargeRepository;
    private final OrderStaffAllocationRepository  staffAllocationRepository;
    private final StaffProfileRepository          staffProfileRepository;

    private static final Color NAVY       = new Color(15, 33, 55);
    private static final Color LIGHT_BLUE = new Color(232, 240, 254);
    private static final Color GRAY       = new Color(90, 105, 120);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generatePdf(Long lrId) {
        Lr   lr         = lrRepository.findById(lrId).orElseThrow(() -> new RuntimeException("LR not found"));
        List<LrCheckpost> checkposts = lrCheckpostRepository.findByLrIdAndIsActiveTrue(lrId);
        List<LrCharge>    charges    = lrChargeRepository.findByLrIdAndIsActiveTrue(lrId);

        Tenant  tenant  = lr.getTenant();
        Order   order   = lr.getOrder();
        Vehicle vehicle = lr.getVehicleAllocation().getVehicle();

        // ── Resolve driver name + DL number ───────────────────────────────
        String driverName = "";
        String driverDl   = "";
        List<OrderStaffAllocation> staffList =
                staffAllocationRepository.findByVehicleAllocationIdAndIsActiveTrue(lr.getVehicleAllocation().getId());
        for (OrderStaffAllocation sa : staffList) {
            if (sa.getRole() != null && RoleName.DRIVER.equals(sa.getRole().getName())) {
                driverName = sa.getUser().getName();
                staffProfileRepository.findByUserId(sa.getUser().getId()).ifPresent(p -> {
                    // capture via field — lambda can't assign to local var
                });
                StaffProfile profile = staffProfileRepository.findByUserId(sa.getUser().getId()).orElse(null);
                if (profile != null && profile.getLicenseNumber() != null) driverDl = profile.getLicenseNumber();
                break;
            }
        }
        final String finalDriverName = driverName;
        final String finalDriverDl   = driverDl;

        // ── Fonts ─────────────────────────────────────────────────────────
        Font navyBold14 = new Font(Font.HELVETICA, 13, Font.BOLD,   NAVY);
        Font bold8      = new Font(Font.HELVETICA,  7, Font.BOLD,   Color.BLACK);
        Font bold8Navy  = new Font(Font.HELVETICA,  7, Font.BOLD,   NAVY);
        Font bold9Navy  = new Font(Font.HELVETICA,  8, Font.BOLD,   NAVY);
        Font reg7       = new Font(Font.HELVETICA,  7, Font.NORMAL, Color.BLACK);
        Font gray6      = new Font(Font.HELVETICA,  6, Font.NORMAL, GRAY);
        Font small6     = new Font(Font.HELVETICA,  5, Font.NORMAL, GRAY);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A5, 20, 20, 18, 18);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── PAN / GSTIN ───────────────────────────────────────────────
            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            noBorderCell(meta, "PAN : " + blank(tenant.getPanNumber()),  Element.ALIGN_LEFT,  small6);
            noBorderCell(meta, "GSTIN : " + blank(tenant.getGstin()),    Element.ALIGN_RIGHT, small6);
            doc.add(meta);

            // ── Company name ──────────────────────────────────────────────
            Paragraph companyName = new Paragraph(tenant.getCompanyName(), navyBold14);
            companyName.setAlignment(Element.ALIGN_CENTER);
            companyName.setSpacingBefore(1);
            companyName.setSpacingAfter(1);
            doc.add(companyName);

            if (tenant.getAddress() != null) {
                Paragraph addr = new Paragraph(tenant.getAddress(), gray6);
                addr.setAlignment(Element.ALIGN_CENTER);
                addr.setSpacingAfter(2);
                doc.add(addr);
            }

            // ── LORRY RECEIPT divider ─────────────────────────────────────
            PdfPTable titleBar = new PdfPTable(1);
            titleBar.setWidthPercentage(100);
            titleBar.setSpacingAfter(3);
            PdfPCell titleCell = new PdfPCell(new Phrase("LORRY RECEIPT",
                    new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE)));
            titleCell.setBackgroundColor(NAVY);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(3);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleBar.addCell(titleCell);
            doc.add(titleBar);

            // ── INFO SECTION ──────────────────────────────────────────────
            String fromCity  = order.getSourceCity()      != null ? order.getSourceCity().getName()      : "";
            String toCity    = order.getDestinationCity() != null ? order.getDestinationCity().getName() : "";
            String fromState = order.getSourceState()     != null ? order.getSourceState().getName()     : "";
            String toState   = order.getDestinationState()!= null ? order.getDestinationState().getName(): "";
            String fromLabel = (order.getSourceAddress() != null && !order.getSourceAddress().isBlank())
                    ? order.getSourceAddress()
                    : (fromCity.isEmpty() ? "—" : fromCity);
            String toLabel   = (order.getDestinationAddress() != null && !order.getDestinationAddress().isBlank())
                    ? order.getDestinationAddress()
                    : (toCity.isEmpty() ? "—" : toCity);

            PdfPTable info = new PdfPTable(new float[]{2.3f, 1f});
            info.setWidthPercentage(100);

            // ── Left panel: Consignor / Consignee / From-To ───────────────
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.BOX); left.setBorderColor(NAVY); left.setPadding(5);

            // Consignor
            left.addElement(new Paragraph("Consignor's Name & Address", bold8));
            String consignorLine = fromCity.isEmpty() ? "—" : fromCity + (fromState.isEmpty() ? "" : ", " + fromState);
            Paragraph consignorVal = new Paragraph(consignorLine, bold8Navy);
            consignorVal.setSpacingBefore(3);
            left.addElement(consignorVal);
            if (order.getSourceAddress() != null && !order.getSourceAddress().isBlank())
                left.addElement(new Paragraph(order.getSourceAddress(), gray6));

            // Consignee
            Paragraph consigneeLabel = new Paragraph("Consignee Name & Address", bold8);
            consigneeLabel.setSpacingBefore(7);
            left.addElement(consigneeLabel);
            Paragraph consigneeVal = new Paragraph(order.getClient().getClientName(), bold8Navy);
            consigneeVal.setSpacingBefore(3);
            left.addElement(consigneeVal);
            String consigneeCityLine = toCity.isEmpty() ? "" : toCity + (toState.isEmpty() ? "" : ", " + toState);
            if (!consigneeCityLine.isEmpty()) left.addElement(new Paragraph(consigneeCityLine, gray6));
            if (order.getDestinationAddress() != null && !order.getDestinationAddress().isBlank())
                left.addElement(new Paragraph(order.getDestinationAddress(), gray6));

            // From → To
            Paragraph fromTo = new Paragraph();
            fromTo.setSpacingBefore(7);
            fromTo.add(new Chunk("From  ", bold8));
            fromTo.add(new Chunk(fromLabel, bold8Navy));
            fromTo.add(new Chunk("    To  ", bold8));
            fromTo.add(new Chunk(toLabel, bold8Navy));
            left.addElement(fromTo);
            info.addCell(left);

            // ── Right panel: LR No / Date / Vehicle No ────────────────────
            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.BOX); right.setBorderColor(NAVY); right.setPadding(0);

            PdfPTable rightInner = new PdfPTable(1);
            rightInner.setWidthPercentage(100);

            // LR Number (highlighted)
            PdfPCell lrNoCell = new PdfPCell();
            lrNoCell.setBorderWidthBottom(0.8f); lrNoCell.setBorderColorBottom(NAVY);
            lrNoCell.setBorderWidthTop(0); lrNoCell.setBorderWidthLeft(0); lrNoCell.setBorderWidthRight(0);
            lrNoCell.setPadding(5); lrNoCell.setMinimumHeight(20);
            lrNoCell.addElement(new Paragraph("No.", gray6));
            lrNoCell.addElement(new Paragraph(lr.getLrNumber(), bold9Navy));
            rightInner.addCell(lrNoCell);

            // Date
            String lrDateStr = lr.getLrDate() != null ? lr.getLrDate().format(DATE_FMT) : "—";
            PdfPCell dateCell = new PdfPCell();
            dateCell.setBorderWidthBottom(0.8f); dateCell.setBorderColorBottom(NAVY);
            dateCell.setBorderWidthTop(0); dateCell.setBorderWidthLeft(0); dateCell.setBorderWidthRight(0);
            dateCell.setPadding(5); dateCell.setMinimumHeight(18);
            dateCell.addElement(new Paragraph("Date", gray6));
            dateCell.addElement(new Paragraph(lrDateStr, bold8));
            rightInner.addCell(dateCell);

            // Vehicle No
            PdfPCell vehCell = new PdfPCell();
            vehCell.setBorderWidthBottom(0.8f); vehCell.setBorderColorBottom(NAVY);
            vehCell.setBorderWidthTop(0); vehCell.setBorderWidthLeft(0); vehCell.setBorderWidthRight(0);
            vehCell.setPadding(5); vehCell.setMinimumHeight(20);
            vehCell.addElement(new Paragraph("Vehicle No.", gray6));
            vehCell.addElement(new Paragraph(vehicle.getRegistrationNumber(), bold9Navy));
            if (vehicle.getVehicleType() != null)
                vehCell.addElement(new Paragraph(vehicle.getVehicleType().getName(), gray6));
            rightInner.addCell(vehCell);

            // E-Way Bill (if present)
            if (lr.getEwayBillNumber() != null && !lr.getEwayBillNumber().isBlank()) {
                PdfPCell ewayCell = new PdfPCell();
                ewayCell.setBorderWidthBottom(0); ewayCell.setBorderWidthTop(0);
                ewayCell.setBorderWidthLeft(0); ewayCell.setBorderWidthRight(0);
                ewayCell.setPadding(5); ewayCell.setMinimumHeight(18);
                ewayCell.addElement(new Paragraph("E-Way Bill", gray6));
                ewayCell.addElement(new Paragraph(lr.getEwayBillNumber(), bold8));
                rightInner.addCell(ewayCell);
            } else {
                // Empty filler so right panel matches left panel height
                PdfPCell filler = new PdfPCell(new Phrase(" "));
                filler.setBorder(Rectangle.NO_BORDER);
                filler.setMinimumHeight(18);
                rightInner.addCell(filler);
            }

            right.addElement(rightInner);
            info.addCell(right);
            doc.add(info);

            // ── FREIGHT TABLE ─────────────────────────────────────────────
            // Columns: No.of Pkg | Material | Actual Wt | Charged Wt | Rate | Rs. | Ps. | Remarks
            PdfPTable table = new PdfPTable(new float[]{0.55f, 2.0f, 0.75f, 0.75f, 1.4f, 0.75f, 0.45f, 1.2f});
            table.setWidthPercentage(100);

            // Header row 1 (spanning)
            th(table, "No. of\nPackage");
            th(table, "Material");
            table.addCell(span2Header("Weight"));
            th(table, "Rate");
            table.addCell(span2Header("Freight"));
            th(table, "Remarks");

            // Header row 2 (sub-headers)
            th(table, " "); th(table, " ");
            th(table, "Actual"); th(table, "Charged");
            th(table, " ");
            th(table, "Rs."); th(table, "Ps.");
            th(table, " ");

            // ── Freight calculation ───────────────────────────────────────
            BigDecimal loadedWt   = zero(lr.getLoadedWeight());
            BigDecimal billedWt   = "LOADED_WEIGHT".equals(order.getBillingOn().name())
                    ? loadedWt
                    : zero(lr.getDeliveredWeight() != null ? lr.getDeliveredWeight() : lr.getLoadedWeight());

            BigDecimal freightAmt = "PER_TON".equals(order.getFreightRateType().name())
                    ? order.getFreightRate().multiply(billedWt)
                    : order.getFreightRate();

            int fRs = freightAmt.intValue();
            int fPs = freightAmt.subtract(BigDecimal.valueOf(fRs))
                    .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();

            String rateTypeStr = order.getFreightRateType().name().equals("PER_TON") ? "/ TON" : "/ TRIP";
            String rateText    = "Rs. " + fmt(order.getFreightRate()) + " " + rateTypeStr;
            String material    = order.getMaterialType() != null ? order.getMaterialType().getName() : "—";
            String actualWtStr = loadedWt.compareTo(BigDecimal.ZERO) > 0 ? fmt(loadedWt) + " T" : "—";
            String billedWtStr = billedWt.compareTo(BigDecimal.ZERO) > 0 ? fmt(billedWt) + " T" : "—";

            // Data row
            td(table, "1",         Element.ALIGN_CENTER, reg7);
            tdLeft(table, material, bold8);
            td(table, actualWtStr, Element.ALIGN_CENTER, reg7);
            td(table, billedWtStr, Element.ALIGN_CENTER, reg7);
            td(table, rateText,    Element.ALIGN_CENTER, new Font(Font.HELVETICA, 6.5f, Font.NORMAL, Color.BLACK));
            td(table, fRs > 0 ? String.format("%,d", fRs) : "—", Element.ALIGN_RIGHT, bold8);
            td(table, String.format("%02d", fPs),                  Element.ALIGN_CENTER, reg7);
            tdLeft(table, blank(lr.getRemarks()), gray6);

            // Extra charges rows
            BigDecimal totalCharges = BigDecimal.ZERO;
            for (LrCharge ch : charges) {
                totalCharges = totalCharges.add(ch.getAmount());
                String chName = ch.getChargeType() != null ? ch.getChargeType().getName() : "Extra Charge";
                int chRs = ch.getAmount().intValue();
                int chPs = ch.getAmount().subtract(BigDecimal.valueOf(chRs))
                        .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
                td(table, "—",   Element.ALIGN_CENTER, reg7);
                tdLeft(table, chName, gray6);
                td(table, "—",   Element.ALIGN_CENTER, reg7);
                td(table, "—",   Element.ALIGN_CENTER, reg7);
                td(table, chName, Element.ALIGN_CENTER, gray6);
                td(table, String.format("%,d", chRs), Element.ALIGN_RIGHT, bold8);
                td(table, String.format("%02d", chPs), Element.ALIGN_CENTER, reg7);
                tdLeft(table, " ", reg7);
            }

            // Checkpost fines row (aggregated)
            BigDecimal totalFines = checkposts.stream()
                    .filter(cp -> cp.getFineAmount() != null)
                    .map(LrCheckpost::getFineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalFines.compareTo(BigDecimal.ZERO) > 0) {
                int cpRs = totalFines.intValue();
                int cpPs = totalFines.subtract(BigDecimal.valueOf(cpRs))
                        .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
                td(table, "—",  Element.ALIGN_CENTER, reg7);
                tdLeft(table, "Checkpost Fines (" + checkposts.size() + ")", gray6);
                td(table, "—",  Element.ALIGN_CENTER, reg7);
                td(table, "—",  Element.ALIGN_CENTER, reg7);
                td(table, "Fines", Element.ALIGN_CENTER, gray6);
                td(table, String.format("%,d", cpRs), Element.ALIGN_RIGHT, bold8);
                td(table, String.format("%02d", cpPs), Element.ALIGN_CENTER, reg7);
                tdLeft(table, " ", reg7);
            }

            // ── Grand total row ───────────────────────────────────────────
            BigDecimal grandTotal = freightAmt.add(totalCharges).add(totalFines);
            int gRs = grandTotal.intValue();
            int gPs = grandTotal.subtract(BigDecimal.valueOf(gRs))
                    .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();

            PdfPCell totalLbl = new PdfPCell(new Phrase("Total LR Value", bold8));
            totalLbl.setColspan(5); totalLbl.setBackgroundColor(LIGHT_BLUE); totalLbl.setBorderColor(NAVY);
            totalLbl.setHorizontalAlignment(Element.ALIGN_RIGHT); totalLbl.setPadding(4);
            table.addCell(totalLbl);

            PdfPCell gRsCell = new PdfPCell(new Phrase(String.format("%,d", gRs),
                    new Font(Font.HELVETICA, 7, Font.BOLD, NAVY)));
            gRsCell.setBackgroundColor(LIGHT_BLUE); gRsCell.setBorderColor(NAVY);
            gRsCell.setHorizontalAlignment(Element.ALIGN_RIGHT); gRsCell.setPadding(4);
            table.addCell(gRsCell);

            PdfPCell gPsCell = new PdfPCell(new Phrase(String.format("%02d", gPs), reg7));
            gPsCell.setBackgroundColor(LIGHT_BLUE); gPsCell.setBorderColor(NAVY);
            gPsCell.setHorizontalAlignment(Element.ALIGN_CENTER); gPsCell.setPadding(4);
            table.addCell(gPsCell);

            PdfPCell remarksFiller = new PdfPCell(new Phrase(" ", reg7));
            remarksFiller.setBackgroundColor(LIGHT_BLUE); remarksFiller.setBorderColor(NAVY);
            remarksFiller.setPadding(4);
            table.addCell(remarksFiller);

            doc.add(table);

            // ── FOOTER ───────────────────────────────────────────────────
            PdfPTable footer = new PdfPTable(new float[]{1.4f, 1f});
            footer.setWidthPercentage(100);
            footer.setSpacingBefore(0);

            PdfPCell footLeft = new PdfPCell();
            footLeft.setBorderColor(NAVY); footLeft.setPadding(6);

            footLeft.addElement(new Paragraph("Owner M/s. " + tenant.getCompanyName(), bold8));

            // Driver name
            Paragraph driverLine = new Paragraph();
            driverLine.setSpacingBefore(7);
            driverLine.add(new Chunk("Driver : ", bold8));
            driverLine.add(new Chunk(finalDriverName.isEmpty() ? "____________________________" : finalDriverName, bold8Navy));
            footLeft.addElement(driverLine);

            // D/L number
            Paragraph dlLine = new Paragraph();
            dlLine.setSpacingBefore(5);
            dlLine.add(new Chunk("D/L. No. : ", bold8));
            dlLine.add(new Chunk(finalDriverDl.isEmpty() ? "______________________" : finalDriverDl, bold8Navy));
            footLeft.addElement(dlLine);

            // Declared value
            Paragraph declaredLine = new Paragraph();
            declaredLine.setSpacingBefore(5);
            declaredLine.add(new Chunk("Declared Value Rs. : ", bold8));
            declaredLine.add(new Chunk("______________________________", gray6));
            footLeft.addElement(declaredLine);

            footer.addCell(footLeft);

            PdfPCell footRight = new PdfPCell();
            footRight.setBorderColor(NAVY); footRight.setPadding(6);
            footRight.setVerticalAlignment(Element.ALIGN_TOP);
            footRight.addElement(new Paragraph("For " + tenant.getCompanyName(), bold8));
            Paragraph supervisorSig = new Paragraph("\n\n\nAuthorised Signatory", gray6);
            supervisorSig.setAlignment(Element.ALIGN_RIGHT);
            footRight.addElement(supervisorSig);
            footer.addCell(footRight);

            doc.add(footer);

            // ── FEROS watermark line ──────────────────────────────────────
            Paragraph gen = new Paragraph("Powered by FEROS", small6);
            gen.setAlignment(Element.ALIGN_CENTER);
            gen.setSpacingBefore(3);
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
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(3);
        t.addCell(c);
    }

    private PdfPCell span2Header(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 6.5f, Font.BOLD, Color.BLACK)));
        c.setColspan(2);
        c.setBackgroundColor(LIGHT_BLUE);
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(3);
        return c;
    }

    private void td(PdfPTable t, String text, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(3);
        c.setMinimumHeight(20);
        t.addCell(c);
    }

    private void tdLeft(PdfPTable t, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorderColor(NAVY);
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(3);
        c.setMinimumHeight(20);
        t.addCell(c);
    }

    /** Strip trailing zeros, e.g. 250.00 → "250", 12500.50 → "12,500.50" */
    private String fmt(BigDecimal v) {
        if (v == null) return "0";
        BigDecimal stripped = v.stripTrailingZeros();
        if (stripped.scale() <= 0) return String.format("%,d", stripped.longValue());
        return String.format("%,.2f", v);
    }

    private String blank(String s) { return s != null ? s : ""; }
    private BigDecimal zero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
}
