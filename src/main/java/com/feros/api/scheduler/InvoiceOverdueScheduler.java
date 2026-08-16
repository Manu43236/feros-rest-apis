package com.feros.api.scheduler;

import com.feros.api.util.TimeUtil;
import com.feros.api.entity.Invoice;
import com.feros.api.entity.Tenant;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.enums.NotificationType;
import com.feros.api.enums.RoleName;
import com.feros.api.repository.InvoiceRepository;
import com.feros.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceOverdueScheduler {

    private final InvoiceRepository invoiceRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 1 * * *") // 1 AM daily
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = TimeUtil.today();
        List<Invoice> invoices = invoiceRepository.findByStatusInAndDueDateBefore(
                List.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID), today);

        if (invoices.isEmpty()) return;

        invoices.forEach(i -> i.setInvoiceStatus(InvoiceStatus.OVERDUE));
        invoiceRepository.saveAll(invoices);
        log.info("Marked {} invoice(s) as OVERDUE", invoices.size());

        // Group by tenant and push one summary notification per tenant
        invoices.stream()
                .collect(Collectors.groupingBy(i -> i.getTenant()))
                .forEach((tenant, tenantInvoices) -> {
                    long overdueCount = tenantInvoices.size();
                    BigDecimal totalOutstanding = tenantInvoices.stream()
                            .map(Invoice::getBalanceDue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long oldestDays = tenantInvoices.stream()
                            .mapToLong(i -> ChronoUnit.DAYS.between(i.getDueDate(), today))
                            .max().orElse(0);

                    String body = overdueCount + " invoice(s) overdue. Oldest: " + oldestDays
                            + " day(s). Total outstanding: ₹"
                            + String.format("%,.0f", totalOutstanding) + ". Follow up with clients.";

                    notificationService.sendToRoles(tenant,
                            List.of(RoleName.OFFICE_STAFF, RoleName.ADMIN),
                            NotificationType.INVOICE_OVERDUE,
                            "Overdue Invoices — Action Required",
                            body,
                            Map.of("type", "INVOICE_OVERDUE"));
                });
    }
}
