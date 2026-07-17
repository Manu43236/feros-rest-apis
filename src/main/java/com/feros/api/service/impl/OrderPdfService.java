package com.feros.api.service.impl;

import com.feros.api.dto.response.OrderResponse;
import com.feros.api.dto.response.VehicleAllocationResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPdfService {

    private static final Color NAVY      = new Color(15, 33, 55);
    private static final Color GRAY      = new Color(90, 105, 120);
    private static final Color LIGHT_BG  = new Color(245, 247, 250);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generatePdf(OrderResponse order, String companyName) {
        Font navyBold  = new Font(Font.HELVETICA, 13, Font.BOLD,   NAVY);
        Font bold8     = new Font(Font.HELVETICA,  7, Font.BOLD,   Color.BLACK);
        Font bold8Navy = new Font(Font.HELVETICA,  7, Font.BOLD,   NAVY);
        Font reg7      = new Font(Font.HELVETICA,  7, Font.NORMAL, Color.BLACK);
        Font gray6     = new Font(Font.HELVETICA,  6, Font.NORMAL, GRAY);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A5, 20, 20, 18, 18);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── Company name ──────────────────────────────────────────────
            Paragraph company = new Paragraph(companyName, navyBold);
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(2);
            doc.add(company);

            // ── ORDER COPY title bar ──────────────────────────────────────
            PdfPTable titleBar = new PdfPTable(1);
            titleBar.setWidthPercentage(100);
            titleBar.setSpacingAfter(6);
            PdfPCell titleCell = new PdfPCell(
                    new Phrase("ORDER COPY", new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE)));
            titleCell.setBackgroundColor(NAVY);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(3);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleBar.addCell(titleCell);
            doc.add(titleBar);

            // ── Order meta (2-col grid) ───────────────────────────────────
            PdfPTable meta = new PdfPTable(new float[]{1f, 1f});
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(6);

            addMetaRow(meta, "Order No",   order.getOrderNumber() != null ? order.getOrderNumber() : "—", bold8, reg7);
            addMetaRow(meta, "Date",
                    order.getOrderDate() != null ? order.getOrderDate().format(DATE_FMT) : "—", bold8, reg7);
            addMetaRow(meta, "Client",     str(order.getClientName()),  bold8, reg7);
            addMetaRow(meta, "Status",     str(order.getOrderStatus()), bold8, reg7);
            addMetaRow(meta, "Material",   str(order.getMaterialTypeName()), bold8, reg7);
            addMetaRow(meta, "Route",
                    route(order.getSourceCityName(), order.getDestinationCityName()), bold8, reg7);
            if (order.getRouteName() != null)
                addMetaRow(meta, "Route Name", order.getRouteName(), bold8, reg7);
            doc.add(meta);

            // ── Freight & Weight ──────────────────────────────────────────
            PdfPTable freight = new PdfPTable(new float[]{1f, 1f, 1f});
            freight.setWidthPercentage(100);
            freight.setSpacingAfter(6);
            addFreightCell(freight, "Total Weight",
                    order.getTotalWeight() != null ? order.getTotalWeight() + " T" : "—", bold8, reg7);
            addFreightCell(freight, "Freight Rate",
                    order.getFreightRate() != null
                            ? "₹" + order.getFreightRate() + " / " + rateLabel(order.getFreightRateType())
                            : "—", bold8, reg7);
            addFreightCell(freight, "Bill On",
                    order.getBillingOn() != null
                            ? order.getBillingOn().name().replace("_", " ") : "—", bold8, reg7);
            doc.add(freight);

            // ── Vehicle Allocations ───────────────────────────────────────
            List<VehicleAllocationResponse> allocs = order.getVehicleAllocations();
            if (allocs != null && !allocs.isEmpty()) {
                PdfPTable allocHeader = new PdfPTable(1);
                allocHeader.setWidthPercentage(100);
                allocHeader.setSpacingAfter(3);
                PdfPCell hdrCell = new PdfPCell(
                        new Phrase("Vehicle Assignments", new Font(Font.HELVETICA, 6, Font.BOLD, Color.WHITE)));
                hdrCell.setBackgroundColor(GRAY);
                hdrCell.setPadding(3);
                hdrCell.setBorder(Rectangle.NO_BORDER);
                allocHeader.addCell(hdrCell);
                doc.add(allocHeader);

                PdfPTable allocTable = new PdfPTable(new float[]{2f, 1f, 1f, 1.5f});
                allocTable.setWidthPercentage(100);
                allocTable.setSpacingAfter(6);
                allocTableHeader(allocTable, bold8);
                for (VehicleAllocationResponse a : allocs) {
                    allocRow(allocTable, a, reg7, gray6);
                }
                doc.add(allocTable);
            }

            // ── Remarks / Special Instructions ────────────────────────────
            if (order.getSpecialInstructions() != null || order.getRemarks() != null) {
                PdfPTable notes = new PdfPTable(1);
                notes.setWidthPercentage(100);
                if (order.getSpecialInstructions() != null) {
                    addNoteRow(notes, "Special Instructions", order.getSpecialInstructions(), bold8, reg7);
                }
                if (order.getRemarks() != null) {
                    addNoteRow(notes, "Remarks", order.getRemarks(), bold8, reg7);
                }
                doc.add(notes);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate order PDF", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addMetaRow(PdfPTable t, String label, String value, Font labelFont, Font valFont) {
        PdfPCell lc = new PdfPCell();
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(3);
        lc.setBackgroundColor(LIGHT_BG);
        lc.addElement(new Paragraph(label, labelFont));
        lc.addElement(new Paragraph(value, valFont));
        t.addCell(lc);
    }

    private void addFreightCell(PdfPTable t, String label, String value, Font labelFont, Font valFont) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(new Color(200, 210, 220));
        c.setPadding(5);
        c.addElement(new Paragraph(label, labelFont));
        c.addElement(new Paragraph(value, valFont));
        t.addCell(c);
    }

    private void allocTableHeader(PdfPTable t, Font f) {
        for (String h : new String[]{"Vehicle", "Weight", "Status", "Type"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, f));
            c.setBackgroundColor(LIGHT_BG);
            c.setPadding(3);
            c.setBorderColor(new Color(200, 210, 220));
            t.addCell(c);
        }
    }

    private void allocRow(PdfPTable t, VehicleAllocationResponse a, Font reg, Font gray) {
        addAllocCell(t, a.getVehicleRegistrationNumber() != null ? a.getVehicleRegistrationNumber() : "—", reg);
        addAllocCell(t, a.getAllocatedWeight() != null ? a.getAllocatedWeight() + "T" : "—", reg);
        addAllocCell(t, a.getAllocationStatus() != null ? a.getAllocationStatus().name() : "—", reg);
        addAllocCell(t, a.getVehicleTypeName() != null ? a.getVehicleTypeName() : "—", gray);
    }

    private void addAllocCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(3);
        c.setBorderColor(new Color(220, 225, 230));
        t.addCell(c);
    }

    private void addNoteRow(PdfPTable t, String label, String value, Font labelFont, Font valFont) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(new Color(200, 210, 220));
        c.setPadding(5);
        c.addElement(new Paragraph(label, labelFont));
        c.addElement(new Paragraph(value, valFont));
        t.addCell(c);
    }

    private String str(Object o) { return o != null ? o.toString() : "—"; }

    private String route(String from, String to) {
        if (from == null && to == null) return "—";
        return (from != null ? from : "—") + " → " + (to != null ? to : "—");
    }

    private String rateLabel(Object rateType) {
        if (rateType == null) return "—";
        return rateType.toString().replace("PER_", "");
    }
}
