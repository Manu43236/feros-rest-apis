package com.feros.api.service.impl;

import com.feros.api.dto.request.ClientAdvanceRequest;
import com.feros.api.dto.response.ClientAdvanceResponse;
import com.feros.api.entity.Client;
import com.feros.api.entity.ClientAdvance;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.User;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.ClientAdvanceRepository;
import com.feros.api.repository.ClientRepository;
import com.feros.api.repository.TenantRepository;
import com.feros.api.repository.UserRepository;
import com.feros.api.service.ClientAdvanceService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientAdvanceServiceImpl implements ClientAdvanceService {

    private final ClientAdvanceRepository clientAdvanceRepository;
    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Override
    public ClientAdvanceResponse create(ClientAdvanceRequest request) {
        Long tenantId = SecurityUtil.getCurrentTenantId();

        Tenant tenant = tenantRepository.findByIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));

        Client client = clientRepository.findByIdAndTenantIdAndIsActiveTrue(request.getClientId(), tenantId)
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        User createdBy = userRepository.findByIdAndIsActiveTrue(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new FerosException("User not found", HttpStatus.NOT_FOUND));

        ClientAdvance advance = ClientAdvance.builder()
                .tenant(tenant)
                .client(client)
                .receivedDate(request.getReceivedDate())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .isAdjusted(false)
                .adjustedAmount(BigDecimal.ZERO)
                .createdBy(createdBy)
                .isActive(true)
                .build();

        return mapToResponse(clientAdvanceRepository.save(advance));
    }

    @Override
    public List<ClientAdvanceResponse> getAll() {
        return clientAdvanceRepository
                .findByTenantIdAndIsActiveTrueOrderByReceivedDateDesc(SecurityUtil.getCurrentTenantId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<ClientAdvanceResponse> getByClient(Long clientId) {
        return clientAdvanceRepository
                .findByTenantIdAndClientIdAndIsActiveTrueOrderByReceivedDateDesc(
                        SecurityUtil.getCurrentTenantId(), clientId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public ClientAdvanceResponse getById(Long id) {
        return mapToResponse(clientAdvanceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Advance not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public void delete(Long id) {
        ClientAdvance advance = clientAdvanceRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, SecurityUtil.getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Advance not found", HttpStatus.NOT_FOUND));
        if (Boolean.TRUE.equals(advance.getIsAdjusted())) {
            throw new FerosException("Cannot delete an adjusted advance", HttpStatus.BAD_REQUEST);
        }
        advance.setIsActive(false);
        clientAdvanceRepository.save(advance);
    }

    private ClientAdvanceResponse mapToResponse(ClientAdvance a) {
        BigDecimal adjusted = a.getAdjustedAmount() != null ? a.getAdjustedAmount() : BigDecimal.ZERO;
        return ClientAdvanceResponse.builder()
                .id(a.getId())
                .tenantId(a.getTenant().getId())
                .clientId(a.getClient().getId())
                .clientName(a.getClient().getClientName())
                .receivedDate(a.getReceivedDate())
                .amount(a.getAmount())
                .paymentMode(a.getPaymentMode())
                .referenceNumber(a.getReferenceNumber())
                .isAdjusted(a.getIsAdjusted())
                .adjustedAmount(adjusted)
                .pendingAmount(a.getAmount().subtract(adjusted))
                .adjustedInvoiceId(a.getAdjustedInvoice() != null ? a.getAdjustedInvoice().getId() : null)
                .adjustedInvoiceNumber(a.getAdjustedInvoice() != null ? a.getAdjustedInvoice().getInvoiceNumber() : null)
                .adjustedAt(a.getAdjustedAt())
                .remarks(a.getRemarks())
                .createdById(a.getCreatedBy().getId())
                .createdByName(a.getCreatedBy().getName())
                .isActive(a.getIsActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
