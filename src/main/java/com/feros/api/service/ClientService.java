package com.feros.api.service;

import com.feros.api.dto.request.ClientRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.ClientDivisionResponse;
import com.feros.api.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientService {
    ClientResponse createClient(ClientRequest request);
    ClientResponse getClientById(Long id);
    Page<ClientResponse> getAllClients(int page, int size, String search);
    ClientResponse updateClient(Long id, ClientRequest request);
    void deleteClient(Long id);
    ClientResponse toggleStatus(Long id, Boolean isActive);
    BulkTenantUploadResponse bulkUpload(MultipartFile file);

    // Divisions
    List<ClientDivisionResponse> getDivisions(Long clientId);
    ClientDivisionResponse addDivision(Long clientId, String name);
    void deleteDivision(Long clientId, Long divisionId);
}