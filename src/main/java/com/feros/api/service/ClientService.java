package com.feros.api.service;

import com.feros.api.dto.request.ClientRequest;
import com.feros.api.dto.response.BulkTenantUploadResponse;
import com.feros.api.dto.response.ClientResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientService {
    ClientResponse createClient(ClientRequest request);
    ClientResponse getClientById(Long id);
    List<ClientResponse> getAllClients();
    ClientResponse updateClient(Long id, ClientRequest request);
    void deleteClient(Long id);
    ClientResponse toggleStatus(Long id, Boolean isActive);
    BulkTenantUploadResponse bulkUpload(MultipartFile file);
}