package com.feros.api.controller;

import com.feros.api.dto.response.ApiResponse;
import com.feros.api.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3Service s3Service;

    /**
     * Upload any file.
     * folder param controls the S3 path, e.g.:
     *   tenants/5/logo
     *   tenants/5/staff/12/documents
     *   tenants/5/vehicles/8/documents
     *   tenants/5/trip-proofs/99
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) throws IOException {

        String key = s3Service.uploadFile(file, folder);
        String url = s3Service.generatePresignedUrl(key);
        String publicUrl = s3Service.getPublicUrl(key);

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully",
                Map.of("key", key, "url", url, "publicUrl", publicUrl)));
    }

    /**
     * Get a fresh pre-signed URL for an existing S3 key (valid 1 hour).
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> presignedUrl(
            @RequestParam("key") String key) {

        String url = s3Service.generatePresignedUrl(key);
        return ResponseEntity.ok(ApiResponse.success("URL generated", Map.of("url", url)));
    }
}
