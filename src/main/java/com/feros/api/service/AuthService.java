package com.feros.api.service;

import com.feros.api.dto.request.ChangePinRequest;
import com.feros.api.dto.request.LoginRequest;
import com.feros.api.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request, String ipAddress);
    void logout();
    void changePin(ChangePinRequest request);
    void askPinReset(String phone);
}