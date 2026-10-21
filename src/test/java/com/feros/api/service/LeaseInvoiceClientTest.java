package com.feros.api.service;

import com.feros.api.dto.request.LeaseInvoiceClientRequest;
import com.feros.api.dto.request.LeaseInvoiceItemRequest;
import com.feros.api.dto.response.LeaseInvoicePrefillResponse;
import com.feros.api.dto.response.LeaseInvoiceResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.RateType;
import com.feros.api.repository.*;
import com.feros.api.service.impl.LeaseInvoiceServiceImpl;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Covers the client-level lease invoice path: cross-lease prefill + null-lease create. */
@ExtendWith(MockitoExtension.class)
class LeaseInvoiceClientTest {

    @Mock LeaseInvoiceRepository invoiceRepository;
    @Mock VehicleLeaseRepository leaseRepository;
    @Mock LeaseVehicleAssignmentRepository assignmentRepository;
    @Mock TenantRepository tenantRepository;
    @Mock ClientRepository clientRepository;
    @Mock NumberGeneratorService numberGenerator;

    LeaseInvoiceServiceImpl service;

    private static final Long TENANT = 1L;
    private static final Long CLIENT = 7L;

    @BeforeEach
    void setUp() {
        service = new LeaseInvoiceServiceImpl(invoiceRepository, leaseRepository,
                assignmentRepository, tenantRepository, clientRepository, numberGenerator);
    }

    private VehicleLease lease(Long id, String number) {
        Client c = Client.builder().id(CLIENT).clientName("Acme").build();
        VehicleLease l = VehicleLease.builder()
                .id(id).leaseNumber(number).client(c).rateType(RateType.DAILY_SHIFT)
                .startDate(LocalDate.of(2026, 1, 1)).build();
        return l;
    }

    private LeaseVehicleAssignment assignment(Long id, VehicleLease l, String reg, BigDecimal rate) {
        Vehicle v = new Vehicle();
        v.setRegistrationNumber(reg);
        return LeaseVehicleAssignment.builder()
                .id(id).lease(l).vehicle(v).ratePerVehicle(rate)
                .startDate(LocalDate.of(2026, 1, 1)).build();
    }

    @Test
    @DisplayName("prefillByClient flattens vehicle lines across all the client's leases")
    void prefillByClient_spansLeases() {
        VehicleLease l1 = lease(10L, "LSE-1");
        VehicleLease l2 = lease(11L, "LSE-2");
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 10); // 10 inclusive days

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(TENANT);
            when(invoiceRepository.findInvoicedAssignments(CLIENT, TENANT, from, to)).thenReturn(List.of());
            when(leaseRepository.findByClient_IdAndTenantIdAndIsActiveTrue(CLIENT, TENANT))
                    .thenReturn(List.of(l1, l2));
            when(assignmentRepository.findByLeaseIdOrderByStartDateAsc(10L))
                    .thenReturn(List.of(assignment(100L, l1, "AP01AA1111", new BigDecimal("1000"))));
            when(assignmentRepository.findByLeaseIdOrderByStartDateAsc(11L))
                    .thenReturn(List.of(assignment(101L, l2, "AP01BB2222", new BigDecimal("500"))));

            List<LeaseInvoicePrefillResponse> lines = service.prefillByClient(CLIENT, from, to);

            assertThat(lines).hasSize(2);
            assertThat(lines).extracting(LeaseInvoicePrefillResponse::getLeaseNumber)
                    .containsExactlyInAnyOrder("LSE-1", "LSE-2");
            // DAILY_SHIFT = rate * days (10 days)
            assertThat(lines).extracting(LeaseInvoicePrefillResponse::getSuggestedAmount)
                    .containsExactlyInAnyOrder(new BigDecimal("10000.00"), new BigDecimal("5000.00"));
            assertThat(lines).allSatisfy(l -> assertThat(l.isAlreadyInvoiced()).isFalse());
        }
    }

    @Test
    @DisplayName("prefillByClient flags a vehicle already invoiced (non-cancelled) for the period")
    void prefillByClient_marksAlreadyInvoiced() {
        VehicleLease l1 = lease(10L, "LSE-1");
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 10);

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(TENANT);
            // assignment 100 already billed on LSEINV-9; assignment 101 free
            when(invoiceRepository.findInvoicedAssignments(CLIENT, TENANT, from, to))
                    .thenReturn(java.util.Collections.singletonList(new Object[]{100L, "LSEINV/26-27/0009"}));
            when(leaseRepository.findByClient_IdAndTenantIdAndIsActiveTrue(CLIENT, TENANT)).thenReturn(List.of(l1));
            when(assignmentRepository.findByLeaseIdOrderByStartDateAsc(10L)).thenReturn(List.of(
                    assignment(100L, l1, "AP01AA1111", new BigDecimal("1000")),
                    assignment(101L, l1, "AP01BB2222", new BigDecimal("500"))));

            List<LeaseInvoicePrefillResponse> lines = service.prefillByClient(CLIENT, from, to);

            LeaseInvoicePrefillResponse locked = lines.stream().filter(l -> l.getAssignmentId().equals(100L)).findFirst().orElseThrow();
            LeaseInvoicePrefillResponse free   = lines.stream().filter(l -> l.getAssignmentId().equals(101L)).findFirst().orElseThrow();
            assertThat(locked.isAlreadyInvoiced()).isTrue();
            assertThat(locked.getInvoicedInvoiceNumber()).isEqualTo("LSEINV/26-27/0009");
            assertThat(free.isAlreadyInvoiced()).isFalse();
        }
    }

    @Test
    @DisplayName("createForClient rejects a vehicle already invoiced for the period")
    void createForClient_rejectsDoubleInvoice() {
        LeaseInvoiceClientRequest req = new LeaseInvoiceClientRequest();
        req.setClientId(CLIENT);
        req.setInvoiceDate(LocalDate.of(2026, 1, 11));
        req.setBillingPeriodStart(LocalDate.of(2026, 1, 1));
        req.setBillingPeriodEnd(LocalDate.of(2026, 1, 10));
        LeaseInvoiceItemRequest i1 = new LeaseInvoiceItemRequest();
        i1.setLeaseVehicleAssignmentId(100L); i1.setRegistrationNumber("AP01AA1111");
        i1.setDays(10); i1.setRate(new BigDecimal("1000")); i1.setAmount(new BigDecimal("10000"));
        req.setItems(List.of(i1));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(TENANT);
            Client c = Client.builder().id(CLIENT).clientName("Acme").build();
            when(clientRepository.findByIdAndTenantIdAndIsActiveTrue(CLIENT, TENANT)).thenReturn(java.util.Optional.of(c));
            when(invoiceRepository.findInvoicedAssignments(CLIENT, TENANT, req.getBillingPeriodStart(), req.getBillingPeriodEnd()))
                    .thenReturn(java.util.Collections.singletonList(new Object[]{100L, "LSEINV/26-27/0009"}));

            org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.createForClient(req))
                    .isInstanceOf(com.feros.api.exception.FerosException.class)
                    .hasMessageContaining("already invoiced");
            verify(invoiceRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("createForClient saves an invoice with null lease and correct totals")
    void createForClient_nullLease_totals() {
        LeaseInvoiceClientRequest req = new LeaseInvoiceClientRequest();
        req.setClientId(CLIENT);
        req.setInvoiceDate(LocalDate.of(2026, 1, 11));
        req.setBillingPeriodStart(LocalDate.of(2026, 1, 1));
        req.setBillingPeriodEnd(LocalDate.of(2026, 1, 10));
        req.setCgstPercentage(new BigDecimal("9"));
        req.setSgstPercentage(new BigDecimal("9"));

        LeaseInvoiceItemRequest i1 = new LeaseInvoiceItemRequest();
        i1.setLeaseVehicleAssignmentId(100L); i1.setRegistrationNumber("AP01AA1111");
        i1.setDays(10); i1.setRate(new BigDecimal("1000")); i1.setAmount(new BigDecimal("10000"));
        LeaseInvoiceItemRequest i2 = new LeaseInvoiceItemRequest();
        i2.setLeaseVehicleAssignmentId(101L); i2.setRegistrationNumber("AP01BB2222");
        i2.setDays(10); i2.setRate(new BigDecimal("500")); i2.setAmount(new BigDecimal("5000"));
        req.setItems(List.of(i1, i2));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentTenantId).thenReturn(TENANT);
            Tenant t = Tenant.builder().id(TENANT).companyName("Me").build();
            Client c = Client.builder().id(CLIENT).clientName("Acme").build();
            when(tenantRepository.findByIdAndIsActiveTrue(TENANT)).thenReturn(java.util.Optional.of(t));
            when(clientRepository.findByIdAndTenantIdAndIsActiveTrue(CLIENT, TENANT)).thenReturn(java.util.Optional.of(c));
            when(invoiceRepository.findInvoicedAssignments(eq(CLIENT), eq(TENANT), any(), any())).thenReturn(List.of());
            when(numberGenerator.generateFY(anyLong(), eq(NumberUtil.Type.LSEINV))).thenReturn("LSEINV/26-27/0001");
            when(invoiceRepository.save(any(LeaseInvoice.class))).thenAnswer(inv -> inv.getArgument(0));

            LeaseInvoiceResponse res = service.createForClient(req);

            assertThat(res.getLeaseId()).isNull();
            assertThat(res.getClientId()).isEqualTo(CLIENT);
            assertThat(res.getSubtotal()).isEqualByComparingTo("15000.00");
            // 18% GST on 15000 = 2700 -> total 17700
            assertThat(res.getTaxAmount()).isEqualByComparingTo("2700.00");
            assertThat(res.getTotalAmount()).isEqualByComparingTo("17700.00");
            assertThat(res.getItems()).hasSize(2);
        }
    }
}
