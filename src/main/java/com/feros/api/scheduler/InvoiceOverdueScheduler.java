package com.feros.api.scheduler;

import com.feros.api.entity.Invoice;
import com.feros.api.enums.InvoiceStatus;
import com.feros.api.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceOverdueScheduler {

    private final InvoiceRepository invoiceRepository;

    @Scheduled(cron = "0 0 1 * * *") // 1 AM daily
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();
        List<Invoice> invoices = invoiceRepository.findByStatusInAndDueDateBefore(
                List.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID), today);

        if (!invoices.isEmpty()) {
            invoices.forEach(i -> i.setInvoiceStatus(InvoiceStatus.OVERDUE));
            invoiceRepository.saveAll(invoices);
            log.info("Marked {} invoice(s) as OVERDUE", invoices.size());
        }
    }
}
