package com.feros.api.service.impl;

import com.feros.api.entity.Payroll;
import com.feros.api.entity.PayrollDeduction;
import com.feros.api.entity.StaffProfile;
import com.feros.api.enums.PayrollStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.PayrollDeductionRepository;
import com.feros.api.repository.PayrollRepository;
import com.feros.api.repository.StaffProfileRepository;
import com.feros.api.util.SecurityUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayslipPdfService {

    private final PayrollRepository payrollRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final StaffProfileRepository staffProfileRepository;

    private static final Color NAVY    = new Color(15, 33, 55);
    private static final Color GRAY    = new Color(90, 105, 120);
    private static final Color SUCCESS = new Color(21, 128, 61);
    private static final Color DANGER  = new Color(185, 28, 28);
    private static final Color LIGHT   = new Color(245, 247, 250);

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private static final Font FONT_HEADER   = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(180, 200, 230));
    private static final Font FONT_SECTION  = new Font(Font.HELVETICA, 9, Font.BOLD, NAVY);
    private static final Font FONT_BODY     = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font FONT_BOLD     = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font FONT_GRAY     = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY);
    private static final Font FONT_NET      = new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE);

    public byte[] generate(Long payrollId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Payroll payroll = payrollRepository.findByIdAndTenantIdAndIsActiveTrue(payrollId, tenantId)
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND));

        if (payroll.getPayrollStatus() != PayrollStatus.PAID) {
            throw new FerosException("Payslip PDF is only available for approved (PAID) payrolls", HttpStatus.BAD_REQUEST);
        }

        List<PayrollDeduction> deductions = payrollDeductionRepository
                .findByPayrollIdAndIsActiveTrue(payrollId);

        StaffProfile profile = staffProfileRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(payroll.getUser().getId(), tenantId)
                .orElse(null);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var tenant = payroll.getTenant();
            var user   = payroll.getUser();
            String companyName = tenant.getCompanyName() != null ? tenant.getCompanyName().toUpperCase() : "FEROS";
            String periodLabel = payroll.getPayCycleStartDate().format(MONTH_FMT);

            // ── Header banner ────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(NAVY);
            hCell.setPadding(16);
            hCell.setBorder(Rectangle.NO_BORDER);
            hCell.addElement(new Phrase(companyName, FONT_HEADER));
            hCell.addElement(new Phrase("Payslip — " + periodLabel, FONT_SUBTITLE));
            header.addCell(hCell);
            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // ── Employee info ────────────────────────────────────────────────
            doc.add(sectionTitle("EMPLOYEE DETAILS"));
            PdfPTable empTable = new PdfPTable(new float[]{1, 1});
            empTable.setWidthPercentage(100);
            addMetaCell(empTable, "Name", user.getName());
            addMetaCell(empTable, "Phone", user.getPhone());

            String role = user.getRoles().stream().findFirst()
                    .map(r -> r.getName().name().replace("_", " ")).orElse("—");
            addMetaCell(empTable, "Role", role);

            String designation = (profile != null && profile.getDesignation() != null)
                    ? profile.getDesignation().getName() : "—";
            addMetaCell(empTable, "Designation", designation);

            addMetaCell(empTable, "Pay Period",
                    payroll.getPayCycleStartDate().format(DATE_FMT) + " to " + payroll.getPayCycleEndDate().format(DATE_FMT));
            addMetaCell(empTable, "Payment Date",
                    payroll.getPaymentDate() != null ? payroll.getPaymentDate().format(DATE_FMT) : "—");
            addMetaCell(empTable, "Payment Mode",
                    payroll.getPaymentMode() != null ? payroll.getPaymentMode().name() : "—");
            addMetaCell(empTable, "Reference No",
                    payroll.getReferenceNumber() != null ? payroll.getReferenceNumber() : "—");
            doc.add(empTable);
            doc.add(Chunk.NEWLINE);

            // ── Attendance summary ───────────────────────────────────────────
            doc.add(sectionTitle("ATTENDANCE SUMMARY"));
            PdfPTable attTable = new PdfPTable(new float[]{1, 1, 1, 1, 1});
            attTable.setWidthPercentage(100);
            addAttHeader(attTable, "Total Days", "Present", "Absent", "Half Days", "Leave Days");
            addAttRow(attTable,
                    str(payroll.getTotalDays()),
                    str(payroll.getPresentDays()),
                    str(payroll.getAbsentDays()),
                    str(payroll.getHalfDays()),
                    str(payroll.getLeaveDays()));
            doc.add(attTable);
            doc.add(Chunk.NEWLINE);

            // ── Earnings ────────────────────────────────────────────────────
            doc.add(sectionTitle("EARNINGS"));
            PdfPTable earnings = new PdfPTable(new float[]{3, 1.5f});
            earnings.setWidthPercentage(100);
            addEarningsHeader(earnings);
            addAmountRow(earnings, "Basic Pay  (Daily Rate: ₹" + fmt(payroll.getDailyRate()) + ")",
                    payroll.getBasicPay(), false, false);
            if (payroll.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
                addAmountRow(earnings, "Overtime Pay  (" + payroll.getOvertimeHours() + " hrs)",
                        payroll.getOvertimePay(), false, false);
            }
            if (payroll.getTripBonus().compareTo(BigDecimal.ZERO) > 0) {
                addAmountRow(earnings, "Trip Bonus", payroll.getTripBonus(), false, false);
            }
            addAmountRow(earnings, "Gross Pay", payroll.getGrossPay(), true, false);
            doc.add(earnings);
            doc.add(Chunk.NEWLINE);

            // ── Deductions ───────────────────────────────────────────────────
            if (!deductions.isEmpty()) {
                doc.add(sectionTitle("DEDUCTIONS"));
                PdfPTable dedTable = new PdfPTable(new float[]{3, 1.5f});
                dedTable.setWidthPercentage(100);
                addEarningsHeader(dedTable);
                for (PayrollDeduction d : deductions) {
                    addAmountRow(dedTable, d.getDeductionType().getName(), d.getAmount(), false, true);
                }
                addAmountRow(dedTable, "Total Deductions", payroll.getTotalDeductions(), true, true);
                doc.add(dedTable);
                doc.add(Chunk.NEWLINE);
            }

            // ── Net Pay banner ───────────────────────────────────────────────
            PdfPTable netBanner = new PdfPTable(1);
            netBanner.setWidthPercentage(100);
            PdfPCell netCell = new PdfPCell();
            netCell.setBackgroundColor(new Color(21, 128, 61));
            netCell.setPadding(14);
            netCell.setBorder(Rectangle.NO_BORDER);
            netCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            netCell.addElement(new Phrase("NET PAY:  ₹" + fmt(payroll.getNetPay()), FONT_NET));
            netBanner.addCell(netCell);
            doc.add(netBanner);

            // ── Remarks ──────────────────────────────────────────────────────
            if (payroll.getRemarks() != null && !payroll.getRemarks().isBlank()) {
                doc.add(Chunk.NEWLINE);
                doc.add(sectionTitle("REMARKS"));
                doc.add(new Paragraph(payroll.getRemarks(), FONT_BODY));
            }

            // ── Footer ───────────────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph(
                    "This is a system-generated payslip. — FEROS Fleet Management", FONT_GRAY);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payslip PDF", e);
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

    private void addAttHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(NAVY);
            cell.setPadding(6);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addAttRow(PdfPTable table, String... vals) {
        for (String v : vals) {
            PdfPCell cell = new PdfPCell(new Phrase(v, FONT_BOLD));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(LIGHT);
            cell.setBorderColor(new Color(220, 228, 240));
            table.addCell(cell);
        }
    }

    private void addEarningsHeader(PdfPTable table) {
        PdfPCell c1 = new PdfPCell(new Phrase("Description", new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        c1.setBackgroundColor(NAVY); c1.setPadding(6); c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase("Amount (₹)", new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        c2.setBackgroundColor(NAVY); c2.setPadding(6); c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void addAmountRow(PdfPTable table, String label, BigDecimal amount, boolean highlight, boolean isDeduction) {
        Color labelColor = highlight ? NAVY : Color.BLACK;
        int labelStyle  = highlight ? Font.BOLD : Font.NORMAL;
        Color amtColor  = highlight ? (isDeduction ? DANGER : SUCCESS) : Color.BLACK;
        int amtStyle    = highlight ? Font.BOLD : Font.NORMAL;

        PdfPCell lc = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, labelStyle, labelColor)));
        lc.setPadding(6);
        lc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        lc.setBorderColor(new Color(220, 228, 240));

        PdfPCell vc = new PdfPCell(new Phrase("₹" + fmt(amount), new Font(Font.HELVETICA, 9, amtStyle, amtColor)));
        vc.setPadding(6);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(highlight ? Rectangle.TOP : Rectangle.BOTTOM);
        vc.setBorderColor(new Color(220, 228, 240));

        table.addCell(lc);
        table.addCell(vc);
    }

    private Paragraph sectionTitle(String title) {
        Paragraph p = new Paragraph(title, FONT_SECTION);
        p.setSpacingBefore(4);
        p.setSpacingAfter(6);
        return p;
    }

    private String fmt(BigDecimal val) {
        return val != null ? val.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00";
    }

    private String str(Integer val) {
        return val != null ? String.valueOf(val) : "0";
    }
}
