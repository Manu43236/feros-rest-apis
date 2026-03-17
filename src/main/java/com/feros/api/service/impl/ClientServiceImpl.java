package com.feros.api.service.impl;

import com.feros.api.dto.request.ClientRequest;
import com.feros.api.dto.response.ClientResponse;
import com.feros.api.entity.Client;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.master.*;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.*;
import com.feros.api.service.ClientService;
import com.feros.api.util.NumberUtil;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;
    private final ClientTypeRepository clientTypeRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final PaymentTermsRepository paymentTermsRepository;

    private Long getCurrentTenantId() {
        return SecurityUtil.getCurrentTenantId();
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findByIdAndIsActiveTrue(getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Tenant not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public ClientResponse createClient(ClientRequest request) {
        Tenant tenant = getCurrentTenant();

        ClientType clientType = clientTypeRepository
                .findByIdAndTenantId(request.getClientTypeId(), tenant.getId())
                .orElseThrow(() -> new FerosException("Client type not found", HttpStatus.NOT_FOUND));

        Client client = Client.builder()
                .tenant(tenant)
                .clientNumber(NumberUtil.generate(tenant.getPrefix(), tenant.getId(), NumberUtil.Type.CLNT))
                .clientName(request.getClientName())
                .clientType(clientType)
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .pincode(request.getPincode())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .contactPersonName(request.getContactPersonName())
                .contactPersonPhone(request.getContactPersonPhone())
                .contactPersonEmail(request.getContactPersonEmail())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.ZERO)
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO)
                .isActive(true)
                .build();

        if (request.getCityId() != null)
            client.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND)));

        if (request.getStateId() != null)
            client.setState(stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND)));

        if (request.getPaymentTermsId() != null)
            client.setPaymentTerms(paymentTermsRepository
                    .findByIdAndTenantId(request.getPaymentTermsId(), tenant.getId())
                    .orElseThrow(() -> new FerosException("Payment terms not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(clientRepository.save(client));
    }

    @Override
    public ClientResponse getClientById(Long id) {
        return mapToResponse(clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<ClientResponse> getAllClients() {
        return clientRepository.findByTenantIdAndIsActiveTrue(getCurrentTenantId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));

        ClientType clientType = clientTypeRepository
                .findByIdAndTenantId(request.getClientTypeId(), getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client type not found", HttpStatus.NOT_FOUND));

        client.setClientName(request.getClientName());
        client.setClientType(clientType);
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        client.setPincode(request.getPincode());
        client.setGstin(request.getGstin());
        client.setPanNumber(request.getPanNumber());
        client.setContactPersonName(request.getContactPersonName());
        client.setContactPersonPhone(request.getContactPersonPhone());
        client.setContactPersonEmail(request.getContactPersonEmail());
        if (request.getCreditLimit() != null)
            client.setCreditLimit(request.getCreditLimit());
        if (request.getOpeningBalance() != null)
            client.setOpeningBalance(request.getOpeningBalance());

        if (request.getCityId() != null)
            client.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new FerosException("City not found", HttpStatus.NOT_FOUND)));

        if (request.getStateId() != null)
            client.setState(stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new FerosException("State not found", HttpStatus.NOT_FOUND)));

        if (request.getPaymentTermsId() != null)
            client.setPaymentTerms(paymentTermsRepository
                    .findByIdAndTenantId(request.getPaymentTermsId(), getCurrentTenantId())
                    .orElseThrow(() -> new FerosException("Payment terms not found", HttpStatus.NOT_FOUND)));

        return mapToResponse(clientRepository.save(client));
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, getCurrentTenantId())
                .orElseThrow(() -> new FerosException("Client not found", HttpStatus.NOT_FOUND));
        client.setIsActive(false);
        clientRepository.save(client);
    }

    private ClientResponse mapToResponse(Client c) {
        return ClientResponse.builder()
                .id(c.getId())
                .tenantId(c.getTenant().getId())
                .clientNumber(c.getClientNumber())
                .clientName(c.getClientName())
                .clientTypeId(c.getClientType().getId())
                .clientTypeName(c.getClientType().getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .cityId(c.getCity() != null ? c.getCity().getId() : null)
                .cityName(c.getCity() != null ? c.getCity().getName() : null)
                .stateId(c.getState() != null ? c.getState().getId() : null)
                .stateName(c.getState() != null ? c.getState().getName() : null)
                .pincode(c.getPincode())
                .gstin(c.getGstin())
                .panNumber(c.getPanNumber())
                .contactPersonName(c.getContactPersonName())
                .contactPersonPhone(c.getContactPersonPhone())
                .contactPersonEmail(c.getContactPersonEmail())
                .paymentTermsId(c.getPaymentTerms() != null ? c.getPaymentTerms().getId() : null)
                .paymentTermsName(c.getPaymentTerms() != null ? c.getPaymentTerms().getName() : null)
                .creditDays(c.getPaymentTerms() != null ? c.getPaymentTerms().getCreditDays() : null)
                .creditLimit(c.getCreditLimit())
                .openingBalance(c.getOpeningBalance())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}