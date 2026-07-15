package com.feros.api.service;

import com.feros.api.config.UserPrincipal;
import com.feros.api.dto.request.CreateInvoiceRequest;
import com.feros.api.entity.*;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.repository.*;
import com.feros.api.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceTaxCalculationTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceLrRepository invoiceLrRepository;
    @Mock private InvoicePaymentRepository invoicePaymentRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private LrRepository lrRepository;
    @Mock private LrChargeRepository lrChargeRepository;
    @Mock private LrCheckpostRepository lrCheckpostRepository;
    @Mock private UserRepository userRepository;
    @Mock private TenantSettingsRepository tenantSettingsRepository;
    @Mock private S3Service s3Service;
    @Mock private NumberGeneratorService numberGenerator;

    private InvoiceServiceImpl invoiceService;

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID   = 1L;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceServiceImpl(
            invoiceRepository, invoiceLrRepository, invoicePaymentRepository,
            tenantRepository, clientRepository, lrRepository,
            lrChargeRepository, lrCheckpostRepository, userRepository,
            tenantSettingsRepository, s3Service, numberGenerator
        );

        UserPrincipal principal = new UserPrincipal(USER_ID, TENANT_ID, "9999999999", "ADMIN");
        var auth = new UsernamePasswordAuthenticationToken(
            principal, null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Inter-state invoice: only IGST applied, no CGST/SGST")
    void createInvoice_interState_appliesIgstOnly() {
        stubDeps(new BigDecimal("10000.00")); // LR freight = 10000

        CreateInvoiceRequest req = buildRequest(new BigDecimal("18"), null, null);
        invoiceService.createInvoice(req);

        Invoice saved = captureInvoiceSave();
        // igstAmt = 10000 × 18% = 1800
        assertThat(saved.getIgstAmount()).isEqualByComparingTo("1800.00");
        assertThat(saved.getCgstAmount()).isEqualByComparingTo("0.00");
        assertThat(saved.getSgstAmount()).isEqualByComparingTo("0.00");
        assertThat(saved.getTaxAmount()).isEqualByComparingTo("1800.00");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("11800.00");
    }

    @Test
    @DisplayName("Intra-state invoice: CGST + SGST applied, no IGST")
    void createInvoice_intraState_appliesCgstAndSgst() {
        stubDeps(new BigDecimal("10000.00"));

        // igst=0, cgst=9%, sgst=9%
        CreateInvoiceRequest req = buildRequest(null, new BigDecimal("9"), new BigDecimal("9"));
        invoiceService.createInvoice(req);

        Invoice saved = captureInvoiceSave();
        assertThat(saved.getCgstAmount()).isEqualByComparingTo("900.00");
        assertThat(saved.getSgstAmount()).isEqualByComparingTo("900.00");
        assertThat(saved.getIgstAmount()).isEqualByComparingTo("0.00");
        assertThat(saved.getTaxAmount()).isEqualByComparingTo("1800.00");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("11800.00");
    }

    @Test
    @DisplayName("Zero-tax invoice: totalAmount equals subtotal")
    void createInvoice_zeroTax_totalEqualSubtotal() {
        stubDeps(new BigDecimal("5000.00"));

        CreateInvoiceRequest req = buildRequest(null, null, null);
        invoiceService.createInvoice(req);

        Invoice saved = captureInvoiceSave();
        assertThat(saved.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getBalanceDue()).isEqualByComparingTo("5000.00");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void stubDeps(BigDecimal lrFreightAmount) {
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setPrefix("TEST");
        when(tenantRepository.findByIdAndIsActiveTrue(TENANT_ID)).thenReturn(Optional.of(tenant));

        Client client = new Client();
        client.setId(10L);
        when(clientRepository.findByIdAndTenantIdAndIsActiveTrue(10L, TENANT_ID))
            .thenReturn(Optional.of(client));

        // LR whose order bills PER_TRIP for simplicity
        Order order = new Order();
        order.setFreightRateType(FreightRateType.PER_TRIP);
        order.setFreightRate(lrFreightAmount);
        order.setBillingOn(BillingOn.LOADED_WEIGHT);
        order.setClient(client);

        Lr lr = new Lr();
        lr.setId(100L);
        lr.setOrder(order);
        lr.setLoadedWeight(new BigDecimal("10.00"));
        lr.setAllocatedWeight(new BigDecimal("10.00"));
        when(invoiceLrRepository.existsByLrIdAndIsActiveTrue(100L)).thenReturn(false);
        when(lrRepository.findByIdAndTenantIdAndIsActiveTrue(100L, TENANT_ID))
            .thenReturn(Optional.of(lr));

        when(lrChargeRepository.findByLrIdAndIsActiveTrue(100L)).thenReturn(List.of());
        when(lrCheckpostRepository.findByLrIdAndIsActiveTrue(100L)).thenReturn(List.of());

        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // invoice.save: first call saves with ZERO amounts (before LRs), second call updates
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));
        when(invoiceLrRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private CreateInvoiceRequest buildRequest(BigDecimal igst, BigDecimal cgst, BigDecimal sgst) {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setClientId(10L);
        req.setLrIds(List.of(100L));
        req.setInvoiceDate(LocalDate.now());
        req.setIgstPercentage(igst);
        req.setCgstPercentage(cgst);
        req.setSgstPercentage(sgst);
        return req;
    }

    private Invoice captureInvoiceSave() {
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, atLeastOnce()).save(captor.capture());
        // The last save has the final computed amounts
        List<Invoice> all = captor.getAllValues();
        return all.get(all.size() - 1);
    }
}
