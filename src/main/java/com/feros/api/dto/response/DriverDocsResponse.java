package com.feros.api.dto.response;

import java.util.List;

public record DriverDocsResponse(
        List<DocumentResponse> myDocs,
        List<DocumentResponse> vehicleDocs
) {}
