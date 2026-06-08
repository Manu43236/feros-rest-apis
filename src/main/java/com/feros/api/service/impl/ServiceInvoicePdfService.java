package com.feros.api.service.impl;

import com.feros.api.entity.ServiceInvoice;
import com.feros.api.entity.ServicePart;
import com.feros.api.entity.SparePartsTransaction;
import com.feros.api.entity.VehicleServiceTask;
import com.feros.api.enums.ServiceInvoiceType;
import com.feros.api.repository.SparePartsTransactionRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceInvoicePdfService {

    private final SparePartsTransactionRepository transactionRepository;

    private static final Color NAVY      = new Color(15, 33, 55);
    private static final Color GRAY      = new Color(90, 105, 120);
    private static final Color SUCCESS   = new Color(21, 128, 61);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
    private static final Font FONT_TITLE  = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);
    private static final Font FONT_BODY   = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font FONT_BOLD   = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font FONT_GRAY   = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY);
    private static final Font FONT_TOTAL  = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);

    public byte[] generate(ServiceInvoice inv, List<ServicePart> approvedParts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var svc    = inv.getService();
            var tenant = inv.getTenant();
            boolean isInternal = inv.getInvoiceType() == ServiceInvoiceType.INTERNAL;

            // ── Header banner ───────────────────────────────────────────────
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(NAVY);
            hCell.setPadding(14);
            hCell.setBorder(Rectangle.NO_BORDER);
            hCell.addElement(new Phrase(tenant.getCompanyName() != null ? tenant.getCompanyName().toUpperCase() : "FEROS", FONT_HEADER));
            hCell.addElement(new Phrase("Service Invoice", new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(180, 200, 230))));
            header.addCell(hCell);
            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // ── Invoice meta ────────────────────────────────────────────────
            PdfPTable meta = new PdfPTable(new float[]{1, 1});
            meta.setWidthPercentage(100);
            addMetaCell(meta, "Invoice No", inv.getInvoiceNumber());
            addMetaCell(meta, "Invoice Date",
                    inv.getCreatedAt() != null ? inv.getCreatedAt().format(DATE_FMT) : "—");
            addMetaCell(meta, "Service No", svc.getServiceNumber());
            addMetaCell(meta, "Service Date",
                    svc.getServiceDate() != null ? svc.getServiceDate().format(DATE_FMT) : "—");
            addMetaCell(meta, "Vehicle", svc.getVehicle().getRegistrationNumber());
            addMetaCell(meta, "Completed",
                    svc.getCompletedDate() != null ? svc.getCompletedDate().format(DATE_FMT) : "—");
            addMetaCell(meta, "Service Type", formatServiceType(svc.getServiceType().name(), svc.getVendorName()));
            addMetaCell(meta, "Payment Status", inv.getPaymentStatus().name());
            doc.add(meta);
            doc.add(Chunk.NEWLINE);

            if (isInternal) {
                // ── Tasks section ────────────────────────────────────────────
                if (!svc.getTasks().isEmpty()) {
                    doc.add(sectionTitle("WORK PERFORMED"));
                    PdfPTable tasks = new PdfPTable(new float[]{4, 1.5f});
                    tasks.setWidthPercentage(100);
                    addTableHeader(tasks, "Task", "Cost (₹)");
                    for (VehicleServiceTask t : svc.getTasks()) {
                        String name = t.getTaskType() != null ? t.getTaskType().getName() : t.getCustomName();
                        BigDecimal cost = t.getCost() != null ? t.getCost() : BigDecimal.ZERO;
                        addTableRow(tasks, name != null ? name : "—", "₹" + cost.setScale(2, RoundingMode.HALF_UP));
                    }
                    doc.add(tasks);
                    doc.add(Chunk.NEWLINE);
                }

                // ── Parts section ────────────────────────────────────────────
                List<ServicePart> parts = approvedParts.stream()
                        .filter(p -> p.getStatus().name().equals("APPROVED")).toList();
                if (!parts.isEmpty()) {
                    // Load transaction costs for each part
                    List<Long> partIds = parts.stream().map(ServicePart::getId).toList();
                    Map<Long, SparePartsTransaction> txByPartId = transactionRepository
                            .findByServicePart_IdIn(partIds).stream()
                            .collect(Collectors.toMap(tx -> tx.getServicePart().getId(), tx -> tx, (a, b) -> a));

                    doc.add(sectionTitle("PARTS USED"));
                    PdfPTable ptable = new PdfPTable(new float[]{3f, 1.5f, 0.8f, 1.2f, 1.2f});
                    ptable.setWidthPercentage(100);
                    addTableHeader(ptable, "Part Name", "Part No", "Qty", "Unit Cost (₹)", "Total (₹)");
                    for (ServicePart p : parts) {
                        int qty = p.getQuantityApproved() != null ? p.getQuantityApproved() : p.getQuantityRequested();
                        SparePartsTransaction tx = txByPartId.get(p.getId());
                        BigDecimal unitCost  = tx != null && tx.getUnitCost()  != null ? tx.getUnitCost()  : BigDecimal.ZERO;
                        BigDecimal totalCost = tx != null && tx.getTotalCost() != null ? tx.getTotalCost() : BigDecimal.ZERO;
                        addPartsRow(ptable,
                                p.getSparePart().getName(),
                                p.getSparePart().getPartNumber() != null ? p.getSparePart().getPartNumber() : "—",
                                qty + " " + p.getSparePart().getUnit(),
                                "₹" + unitCost.setScale(2, RoundingMode.HALF_UP),
                                "₹" + totalCost.setScale(2, RoundingMode.HALF_UP));
                    }
                    doc.add(ptable);
                    doc.add(Chunk.NEWLINE);
                }

                // ── Totals ────────────────────────────────────────────────────
                doc.add(sectionTitle("COST SUMMARY"));
                PdfPTable totals = new PdfPTable(new float[]{3, 1.5f});
                totals.setWidthPercentage(60);
                totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
                addTotalRow(totals, "Tasks Total", inv.getTasksTotal(), false);
                BigDecimal partsTotal = inv.getPartsTotal() != null ? inv.getPartsTotal() : BigDecimal.ZERO;
                if (partsTotal.compareTo(BigDecimal.ZERO) > 0) {
                    addTotalRow(totals, "Parts Total", partsTotal, false);
                }
                addTotalRow(totals, "Labour Charges", inv.getLabourCharges(), false);
                addTotalRow(totals, "Sub Total", inv.getSubTotal(), false);
                addTotalRow(totals, "GST (" + inv.getGstRate().setScale(0, RoundingMode.HALF_UP) + "%)",
                        inv.getGstAmount(), false);
                addTotalRow(totals, "TOTAL", inv.getTotalAmount(), true);
                doc.add(totals);

            } else {
                // ── External / Vendor bill ────────────────────────────────────
                doc.add(sectionTitle("VENDOR DETAILS"));
                PdfPTable vt = new PdfPTable(new float[]{1, 1});
                vt.setWidthPercentage(100);
                addMetaCell(vt, "Vendor Name", svc.getVendorName() != null ? svc.getVendorName() : "—");
                addMetaCell(vt, "Vendor Invoice No",
                        inv.getVendorInvoiceNo() != null ? inv.getVendorInvoiceNo() : "—");
                doc.add(vt);
                doc.add(Chunk.NEWLINE);

                doc.add(sectionTitle("AMOUNT"));
                PdfPTable at = new PdfPTable(new float[]{3, 1.5f});
                at.setWidthPercentage(60);
                at.setHorizontalAlignment(Element.ALIGN_RIGHT);
                BigDecimal total = inv.getVendorAmount() != null ? inv.getVendorAmount() : BigDecimal.ZERO;
                addTotalRow(at, "Vendor Charged (incl. GST)", total, true);
                doc.add(at);
            }

            // ── Footer ─────────────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("This is a system-generated internal service invoice. — FEROS Fleet Management", FONT_GRAY);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate service invoice PDF", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(NAVY);
            cell.setPadding(6);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String col1, String col2) {
        PdfPCell c1 = new PdfPCell(new Phrase(col1, FONT_BODY));
        c1.setPadding(5); c1.setBorderColor(new Color(220, 228, 240));
        PdfPCell c2 = new PdfPCell(new Phrase(col2, FONT_BODY));
        c2.setPadding(5); c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorderColor(new Color(220, 228, 240));
        table.addCell(c1);
        table.addCell(c2);
    }

    private void addTableRow3(PdfPTable table, String c1, String c2, String c3) {
        for (String val : new String[]{c1, c2, c3}) {
            PdfPCell cell = new PdfPCell(new Phrase(val, FONT_BODY));
            cell.setPadding(5); cell.setBorderColor(new Color(220, 228, 240));
            table.addCell(cell);
        }
    }

    private void addPartsRow(PdfPTable table, String name, String partNo, String qty, String unitCost, String total) {
        Color border = new Color(220, 228, 240);
        PdfPCell c1 = new PdfPCell(new Phrase(name, FONT_BODY));
        c1.setPadding(5); c1.setBorderColor(border);
        PdfPCell c2 = new PdfPCell(new Phrase(partNo, FONT_BODY));
        c2.setPadding(5); c2.setBorderColor(border);
        PdfPCell c3 = new PdfPCell(new Phrase(qty, FONT_BODY));
        c3.setPadding(5); c3.setBorderColor(border); c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell c4 = new PdfPCell(new Phrase(unitCost, FONT_BODY));
        c4.setPadding(5); c4.setBorderColor(border); c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell c5 = new PdfPCell(new Phrase(total, FONT_BOLD));
        c5.setPadding(5); c5.setBorderColor(border); c5.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1); table.addCell(c2); table.addCell(c3); table.addCell(c4); table.addCell(c5);
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal amount, boolean highlight) {
        Font lf = highlight ? FONT_TOTAL : FONT_BODY;
        Font vf = highlight ? new Font(Font.HELVETICA, 11, Font.BOLD, SUCCESS) : FONT_BODY;
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setPadding(5); lc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        lc.setBorderColor(new Color(220, 228, 240));
        PdfPCell vc = new PdfPCell(new Phrase("₹" + (amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : "0.00"), vf));
        vc.setPadding(5); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        vc.setBorderColor(new Color(220, 228, 240));
        table.addCell(lc);
        table.addCell(vc);
    }

    private String formatServiceType(String type, String vendor) {
        return switch (type) {
            case "INTERNAL" -> "Internal (Self)";
            case "OEM_CENTER" -> "OEM: " + (vendor != null ? vendor : "Service Center");
            default -> vendor != null ? "3rd Party: " + vendor : "3rd Party";
        };
    }
}
