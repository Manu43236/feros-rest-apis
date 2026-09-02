package com.feros.api.service;

import com.feros.api.dto.request.PostOrderLogRequest;
import com.feros.api.dto.response.OrderResponse;

public interface PostOrderLogService {
    OrderResponse createPostOrderLog(PostOrderLogRequest request);
}
