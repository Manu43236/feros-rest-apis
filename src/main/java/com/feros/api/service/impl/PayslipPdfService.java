package com.feros.api.service.impl;

import com.feros.api.entity.Payroll;
import com.feros.api.entity.PayrollDeduction;
import com.feros.api.entity.StaffProfile;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.PayrollDeductionRepository;
import com.feros.api.repository.PayrollRepository;
import com.feros.api.repository.StaffProfileRepository;
import com.feros.api.repository.AttendanceRepository;
import com.feros.api.repository.VehicleStaffAssignmentRepository;
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
    private final AttendanceRepository attendanceRepository;
    private final VehicleStaffAssignmentRepository vehicleStaffAssignmentRepository;

    private static final Color NAVY       = new Color(15, 33, 55);
    private static final Color NAVY_LIGHT = new Color(30, 58, 95);
    private static final Color ACCENT     = new Color(37, 99, 235);
    private static final Color SUCCESS    = new Color(21, 128, 61);
    private static final Color DANGER     = new Color(185, 28, 28);
    private static final Color MUTED      = new Color(107, 114, 128);
    private static final Color BORDER     = new Color(226, 232, 240);
    private static final Color SURFACE    = new Color(248, 250, 252);
    private static final Color AMBER      = new Color(180, 83, 9);
    private static final Color AMBER_BG   = new Color(254, 243, 199);

    private static final Font F_COMPANY  = new Font(Font.HELVETICA, 18, Font.BOLD,   Color.WHITE);
    private static final Font F_SUBTITLE = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(180, 210, 255));
    private static final Font F_REF      = new Font(Font.HELVETICA,  8, Font.NORMAL, new Color(180, 210, 255));
    private static final Font F_SECTION  = new Font(Font.HELVETICA,  8, Font.BOLD,   NAVY);
    private static final Font F_LABEL    = new Font(Font.HELVETICA,  7, Font.NORMAL, MUTED);
    private static final Font F_VALUE    = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.BLACK);
    private static final Font F_BODY     = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.BLACK);
    private static final Font F_NET      = new Font(Font.HELVETICA, 14, Font.BOLD,   Color.WHITE);
    private static final Font F_NET_LBL  = new Font(Font.HELVETICA,  8, Font.NORMAL, new Color(180, 210, 255));
    private static final Font F_SMALL    = new Font(Font.HELVETICA,  7, Font.NORMAL, MUTED);

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");

    public byte[] generate(Long payrollId) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Payroll payroll = payrollRepository.findByIdAndTenantIdAndIsActiveTrue(payrollId, tenantId)
                .orElseThrow(() -> new FerosException("Payroll not found", HttpStatus.NOT_FOUND));

        List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollIdAndIsActiveTrue(payrollId);

        StaffProfile profile = staffProfileRepository
                .findByUserIdAndTenantIdAndIsActiveTrue(payroll.getUser().getId(), tenantId)
                .orElse(null);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            String companyName = payroll.getTenant().getCompanyName() != null
                    ? payroll.getTenant().getCompanyName().toUpperCase() : "FEROS";
            String periodLabel  = payroll.getPayCycleStartDate().format(MONTH_FMT);
            String designation  = (profile != null && profile.getDesignation() != null)
                    ? profile.getDesignation().getName() : "—";
            String role = payroll.getUser().getRoles().stream().findFirst()
                    .map(r -> r.getName().name().replace("_", " ")).orElse("—");
            boolean isDraft    = "DRAFT".equals(payroll.getPayrollStatus().name());
            boolean isMonthly  = payroll.getSalaryType() != null
                    && "MONTHLY".equals(payroll.getSalaryType().name());

            // ── Header ───────────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(new float[]{3f, 1f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(12);

            PdfPCell hLeft = blankCell(NAVY, Element.ALIGN_LEFT);
            hLeft.setPadding(16);
            hLeft.addElement(new Phrase(companyName, F_COMPANY));
            hLeft.addElement(new Phrase("Payslip  •  " + periodLabel, F_SUBTITLE));
            header.addCell(hLeft);

            PdfPCell hRight = blankCell(NAVY, Element.ALIGN_RIGHT);
            hRight.setPadding(16);
            hRight.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph refP = new Paragraph(payroll.getReferenceNumber() != null ? payroll.getReferenceNumber() : "", F_REF);
            refP.setAlignment(Element.ALIGN_RIGHT);
            Color statusColor = isDraft ? AMBER_BG : new Color(134, 239, 172);
            Paragraph statusP = new Paragraph(payroll.getPayrollStatus().name(),
                    new Font(Font.HELVETICA, 8, Font.BOLD, statusColor));
            statusP.setAlignment(Element.ALIGN_RIGHT);
            hRight.addElement(refP);
            hRight.addElement(statusP);
            header.addCell(hRight);
            doc.add(header);

            // ── Employee info ────────────────────────────────────────────────
            doc.add(sectionLabel("EMPLOYEE INFORMATION"));
            PdfPTable emp = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
            emp.setWidthPercentage(100);
            emp.setSpacingAfter(6);
            addInfoCell(emp, "Name",        payroll.getUser().getName());
            addInfoCell(emp, "Designation", designation);
            addInfoCell(emp, "Pay Period",
                    payroll.getPayCycleStartDate().format(DATE_FMT)
                            + " - " + payroll.getPayCycleEndDate().format(DATE_FMT));
            addInfoCell(emp, "Payment Date",
                    payroll.getPaymentDate() != null ? payroll.getPaymentDate().format(DATE_FMT) : "Pending");
            addInfoCell(emp, "Phone",       payroll.getUser().getPhone());
            addInfoCell(emp, "Role",        role);
            addInfoCell(emp, "Mode",
                    payroll.getPaymentMode() != null ? payroll.getPaymentMode().name() : "Pending");
            addInfoCell(emp, "Reference",
                    payroll.getReferenceNumber() != null ? payroll.getReferenceNumber() : "—");
            doc.add(emp);

            // ── Attendance tiles ─────────────────────────────────────────────
            doc.add(sectionLabel("ATTENDANCE SUMMARY"));
            PdfPTable att = new PdfPTable(5);
            att.setWidthPercentage(100);
            att.setSpacingAfter(6);
            addAttTile(att, str(payroll.getTotalDays()),   "Total Days",  SURFACE, NAVY);
            addAttTile(att, str(payroll.getPresentDays()), "Present",     new Color(219, 234, 254), ACCENT);
            addAttTile(att, str(payroll.getHalfDays()),    "Half Days",   AMBER_BG, AMBER);
            addAttTile(att, str(payroll.getAbsentDays()),  "Absent",
                    payroll.getAbsentDays() > 0 ? new Color(254, 226, 226) : SURFACE,
                    payroll.getAbsentDays() > 0 ? DANGER : MUTED);
            addAttTile(att, str(payroll.getLeaveDays()),   "Leave",       SURFACE, MUTED);
            doc.add(att);

            // ── Earnings + Deductions ────────────────────────────────────────
            doc.add(sectionLabel("PAY SUMMARY"));
            PdfPTable payTable = new PdfPTable(new float[]{1f, 0.04f, 1f});
            payTable.setWidthPercentage(100);
            payTable.setSpacingAfter(6);

            // -- Earnings inner table --
            PdfPTable earningsT = new PdfPTable(new float[]{3f, 1.5f});
            earningsT.setWidthPercentage(100);
            addInnerHeader(earningsT, "EARNINGS", "Amount (Rs.)");

            String basicDesc;
            if (isMonthly) {
                basicDesc = "Basic Salary";
            } else {
                int hd = payroll.getHalfDays();
                int pd = payroll.getPresentDays();
                basicDesc = hd > 0
                        ? "Basic Pay (" + pd + " days + " + hd + " half day" + (hd > 1 ? "s" : "") + " @ Rs." + fmt(payroll.getDailyRate()) + ")"
                        : "Basic Pay (" + pd + " days @ Rs." + fmt(payroll.getDailyRate()) + ")";
            }
            addInnerRow(earningsT, basicDesc, fmt(payroll.getBasicPay()));

            if (payroll.getOvertimePay() != null && payroll.getOvertimePay().compareTo(BigDecimal.ZERO) > 0)
                addInnerRow(earningsT, "Overtime (" + payroll.getOvertimeHours() + " hrs)", fmt(payroll.getOvertimePay()));
            if (payroll.getTripBonus() != null && payroll.getTripBonus().compareTo(BigDecimal.ZERO) > 0)
                addInnerRow(earningsT, "Trip Bonus", fmt(payroll.getTripBonus()));
            if (payroll.getVehicleExtraPay() != null && payroll.getVehicleExtraPay().compareTo(BigDecimal.ZERO) > 0)
                addInnerRow(earningsT, "Vehicle Allowance", fmt(payroll.getVehicleExtraPay()));

            addInnerTotal(earningsT, "GROSS PAY", fmt(payroll.getGrossPay()), SUCCESS);

            PdfPCell earningsCell = new PdfPCell();
            earningsCell.setBorder(Rectangle.BOX);
            earningsCell.setBorderColor(BORDER);
            earningsCell.setPadding(0);
            earningsCell.addElement(earningsT);
            payTable.addCell(earningsCell);

            // gap column
            PdfPCell gap = blankCell(Color.WHITE, Element.ALIGN_LEFT);
            gap.setBorder(Rectangle.NO_BORDER);
            payTable.addCell(gap);

            // -- Deductions inner table --
            PdfPTable deductionsT = new PdfPTable(new float[]{3f, 1.5f});
            deductionsT.setWidthPercentage(100);
            addInnerHeader(deductionsT, "DEDUCTIONS", "Amount (Rs.)");

            if (deductions.isEmpty()) {
                addInnerRow(deductionsT, "No deductions this period", "—");
            } else {
                for (PayrollDeduction d : deductions)
                    addInnerRow(deductionsT, d.getDeductionType().getName(), fmt(d.getAmount()));
            }
            addInnerTotal(deductionsT, "TOTAL DEDUCTIONS", fmt(payroll.getTotalDeductions()), DANGER);

            PdfPCell deductionsCell = new PdfPCell();
            deductionsCell.setBorder(Rectangle.BOX);
            deductionsCell.setBorderColor(BORDER);
            deductionsCell.setPadding(0);
            deductionsCell.addElement(deductionsT);
            payTable.addCell(deductionsCell);
            doc.add(payTable);

            // ── Daily Earnings Annexure (daily-rate only) ────────────────────
            if (!isMonthly) try {
                List<com.feros.api.entity.Attendance> workedDays = attendanceRepository
                        .findByUserIdAndTenantIdAndAttendanceDateBetweenAndIsActiveTrueOrderByAttendanceDateDesc(
                                payroll.getUser().getId(), tenantId,
                                payroll.getPayCycleStartDate(), payroll.getPayCycleEndDate())
                        .stream()
                        .filter(a -> {
                            String t = a.getAttendanceType().getName().toLowerCase();
                            return t.contains("present") || t.contains("half");
                        })
                        .sorted(java.util.Comparator.comparing(com.feros.api.entity.Attendance::getAttendanceDate))
                        .toList();

                if (!workedDays.isEmpty()) {
                    List<com.feros.api.entity.VehicleStaffAssignment> assignments =
                            vehicleStaffAssignmentRepository.findOverlappingByUser(
                                    payroll.getUser().getId(), tenantId,
                                    payroll.getPayCycleStartDate(), payroll.getPayCycleEndDate());

                    doc.add(sectionLabel("DAILY EARNINGS ANNEXURE"));

                    PdfPTable annexure = new PdfPTable(new float[]{1.5f, 1.8f, 1.8f, 1.8f, 1.8f});
                    annexure.setWidthPercentage(100);
                    addAnnexureHeader(annexure, "Date", "Vehicle No.", "Designation Pay", "Vehicle Pay", "Day Total");

                    for (com.feros.api.entity.Attendance record : workedDays) {
                        java.time.LocalDate date = record.getAttendanceDate();
                        boolean isHalf = record.getAttendanceType().getName().toLowerCase().contains("half");
                        BigDecimal factor = isHalf ? new BigDecimal("0.5") : BigDecimal.ONE;
                        BigDecimal desPay = payroll.getDailyRate().multiply(factor).setScale(2, RoundingMode.HALF_UP);

                        String vehicleNo = "—";
                        BigDecimal vehPay = BigDecimal.ZERO;
                        for (com.feros.api.entity.VehicleStaffAssignment a : assignments) {
                            if (!date.isBefore(a.getAssignedFrom())
                                    && (a.getAssignedTo() == null || !date.isAfter(a.getAssignedTo()))) {
                                vehicleNo = a.getVehicle().getRegistrationNumber();
                                if (Boolean.TRUE.equals(a.getVehicle().getExtraPayEnabled())
                                        && a.getVehicle().getExtraPayPerDay() != null) {
                                    vehPay = a.getVehicle().getExtraPayPerDay()
                                            .multiply(factor).setScale(2, RoundingMode.HALF_UP);
                                }
                                break;
                            }
                        }

                        addAnnexureRow(annexure, isHalf,
                                date.format(DATE_FMT),
                                vehicleNo,
                                fmt(desPay),
                                vehPay.compareTo(BigDecimal.ZERO) > 0 ? fmt(vehPay) : "—",
                                fmt(desPay.add(vehPay)));
                    }
                    doc.add(annexure);
                }
            } catch (Exception ignored) { }

            // ── Net Pay banner ───────────────────────────────────────────────
            PdfPTable net = new PdfPTable(new float[]{1f, 1.2f});
            net.setWidthPercentage(100);
            net.setSpacingAfter(14);

            PdfPCell netLabel = blankCell(NAVY_LIGHT, Element.ALIGN_RIGHT);
            netLabel.setPadding(14);
            netLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph netLblP = new Paragraph("NET PAY", F_NET_LBL);
            netLblP.setAlignment(Element.ALIGN_RIGHT);
            netLabel.addElement(netLblP);
            net.addCell(netLabel);

            PdfPCell netAmt = blankCell(NAVY, Element.ALIGN_RIGHT);
            netAmt.setPadding(14);
            netAmt.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph netAmtP = new Paragraph("Rs. " + fmt(payroll.getNetPay()), F_NET);
            netAmtP.setAlignment(Element.ALIGN_RIGHT);
            netAmt.addElement(netAmtP);
            net.addCell(netAmt);
            doc.add(net);

            // ── Remarks ──────────────────────────────────────────────────────
            if (payroll.getRemarks() != null && !payroll.getRemarks().isBlank()) {
                doc.add(sectionLabel("REMARKS"));
                Paragraph rem = new Paragraph(payroll.getRemarks(), F_BODY);
                rem.setSpacingAfter(10);
                doc.add(rem);
            }

            // ── Footer ───────────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "This is a computer-generated payslip and does not require a signature.  •  FEROS Fleet Management",
                    F_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PdfPCell blankCell(Color bg, int hAlign) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(hAlign);
        return c;
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER);
        cell.setPadding(7);
        cell.addElement(new Phrase(label, F_LABEL));
        cell.addElement(new Phrase(value != null ? value : "—", F_VALUE));
        table.addCell(cell);
    }

    private void addAttTile(PdfPTable table, String number, String label, Color bg, Color fg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setPadding(10);
        Paragraph num = new Paragraph(number, new Font(Font.HELVETICA, 16, Font.BOLD, fg));
        num.setAlignment(Element.ALIGN_CENTER);
        Paragraph lbl = new Paragraph(label, new Font(Font.HELVETICA, 7, Font.NORMAL, fg));
        lbl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(num);
        cell.addElement(lbl);
        table.addCell(cell);
    }

    private void addInnerHeader(PdfPTable table, String left, String right) {
        PdfPCell c1 = new PdfPCell(new Phrase(left, new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE)));
        c1.setBackgroundColor(NAVY); c1.setPadding(7); c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(right, new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE)));
        c2.setBackgroundColor(NAVY); c2.setPadding(7); c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void addInnerRow(PdfPTable table, String label, String amount) {
        PdfPCell lc = new PdfPCell(new Phrase(label, F_BODY));
        lc.setPadding(7);
        lc.setBorderWidthBottom(0.5f); lc.setBorderColor(BORDER);
        lc.setBorderWidthTop(0); lc.setBorderWidthLeft(0); lc.setBorderWidthRight(0);

        PdfPCell vc = new PdfPCell(new Phrase(amount, F_BODY));
        vc.setPadding(7);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorderWidthBottom(0.5f); vc.setBorderColor(BORDER);
        vc.setBorderWidthTop(0); vc.setBorderWidthLeft(0); vc.setBorderWidthRight(0);

        table.addCell(lc);
        table.addCell(vc);
    }

    private void addInnerTotal(PdfPTable table, String label, String amount, Color amtColor) {
        PdfPCell lc = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 9, Font.BOLD, NAVY)));
        lc.setPadding(8); lc.setBackgroundColor(SURFACE);
        lc.setBorderWidthTop(1f); lc.setBorderColor(BORDER);
        lc.setBorderWidthBottom(0); lc.setBorderWidthLeft(0); lc.setBorderWidthRight(0);

        PdfPCell vc = new PdfPCell(new Phrase(amount, new Font(Font.HELVETICA, 9, Font.BOLD, amtColor)));
        vc.setPadding(8); vc.setBackgroundColor(SURFACE);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorderWidthTop(1f); vc.setBorderColor(BORDER);
        vc.setBorderWidthBottom(0); vc.setBorderWidthLeft(0); vc.setBorderWidthRight(0);

        table.addCell(lc);
        table.addCell(vc);
    }

    private void addAnnexureHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE)));
            c.setBackgroundColor(NAVY); c.setPadding(6); c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c);
        }
    }

    private void addAnnexureRow(PdfPTable table, boolean isHalf, String... vals) {
        Color bg = isHalf ? AMBER_BG : Color.WHITE;
        for (int i = 0; i < vals.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(vals[i], isHalf
                    ? new Font(Font.HELVETICA, 8, Font.ITALIC, AMBER)
                    : new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK)));
            c.setPadding(5);
            c.setBackgroundColor(bg);
            c.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
            c.setBorderColor(BORDER);
            table.addCell(c);
        }
    }

    private Paragraph sectionLabel(String title) {
        Paragraph p = new Paragraph(title, F_SECTION);
        p.setSpacingBefore(2);
        p.setSpacingAfter(5);
        return p;
    }

    private String fmt(BigDecimal val) {
        return val != null ? String.format("%,.2f", val.setScale(2, RoundingMode.HALF_UP)) : "0.00";
    }

    private String str(Integer val) { return val != null ? String.valueOf(val) : "0"; }
}
