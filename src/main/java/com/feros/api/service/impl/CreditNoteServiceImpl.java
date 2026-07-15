package com.feros.api.service.impl;

import com.feros.api.dto.request.CreditNoteRequest;
import com.feros.api.dto.response.CreditNoteResponse;
import com.feros.api.entity.*;
import com.feros.api.enums.CreditNoteStatus;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.CreditNoteService;
import com.feros.api.service.NumberGeneratorService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditNoteServiceImpl implements CreditNoteService {

    private final InvoiceCreditNoteRepository creditNoteRepository;
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final NumberGeneratorService numberGenerator;

    @Override
    public CreditNoteResponse create(CreditNoteRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenantId)
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        User createdBy = userRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        InvoiceCreditNote.InvoiceCreditNoteBuilder builder = InvoiceCreditNote.builder()
                .tenant(tenant)
                .creditNoteNumber(numberGenerator.generateFY(tenant.getId(), NumberUtil.Type.CN))
                .client(client)
                .creditNoteDate(request.getCreditNoteDate())
                .amount(request.getAmount())
                .reason(request.getReason())
                .remarks(request.getRemarks())
                .creditNoteStatus(CreditNoteStatus.DRAFT)
                .createdBy(createdBy)
                .isActive(true);

        if (request.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findByIdAndTenantIdAndIsActiveTrue(request.getInvoiceId(), tenantId)
                    .orElseThrow(() -> new FerosException("Invoice not found", HttpStatus.NOT_FOUND));
            builder.invoice(invoice);
        }

        return mapToResponse(creditNoteRepository.save(builder.build()));
    }

    @Override
    public List<CreditNoteResponse> getAll() {
        return creditNoteRepository
                .findByTenantIdAndIsActiveTrueOrderByCreditNoteDateDesc(SecurityUtil.getCurrentTenantId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<CreditNoteResponse> getByClient(Long clientId) {
        return creditNoteRepository
                .findByTenantIdAndClientIdAndIsActiveTrueOrderByCreditNoteDateDesc(
                        SecurityUtil.getCurrentTenantId(), clientId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public CreditNoteResponse getById(Long id) {
        return mapToResponse(creditNoteRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Credit note not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public CreditNoteResponse updateStatus(Long id, CreditNoteStatus status) {
        InvoiceCreditNote cn = creditNoteRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Credit note not found", HttpStatus.NOT_FOUND));

        if (cn.getCreditNoteStatus() == CreditNoteStatus.ADJUSTED) {
            throw new FerosException("Cannot change status of an adjusted credit note", HttpStatus.BAD_REQUEST);
        }
        cn.setCreditNoteStatus(status);
        return mapToResponse(creditNoteRepository.save(cn));
    }

    @Override
    public void delete(Long id) {
        InvoiceCreditNote cn = creditNoteRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Credit note not found", HttpStatus.NOT_FOUND));
        if (cn.getCreditNoteStatus() == CreditNoteStatus.ADJUSTED) {
            throw new FerosException("Cannot delete an adjusted credit note", HttpStatus.BAD_REQUEST);
        }
        cn.setIsActive(false);
        creditNoteRepository.save(cn);
    }

    private CreditNoteResponse mapToResponse(InvoiceCreditNote cn) {
        return CreditNoteResponse.builder()
                .id(cn.getId())
                .tenantId(cn.getTenant().getId())
                .creditNoteNumber(cn.getCreditNoteNumber())
                .invoiceId(cn.getInvoice() != null ? cn.getInvoice().getId() : null)
                .invoiceNumber(cn.getInvoice() != null ? cn.getInvoice().getInvoiceNumber() : null)
                .clientId(cn.getClient().getId())
                .clientName(cn.getClient().getClientName())
                .creditNoteDate(cn.getCreditNoteDate())
                .amount(cn.getAmount())
                .reason(cn.getReason())
                .creditNoteStatus(cn.getCreditNoteStatus())
                .remarks(cn.getRemarks())
                .createdById(cn.getCreatedBy().getId())
                .createdByName(cn.getCreatedBy().getName())
                .isActive(cn.getIsActive())
                .createdAt(cn.getCreatedAt())
                .updatedAt(cn.getUpdatedAt())
                .build();
    }
}
