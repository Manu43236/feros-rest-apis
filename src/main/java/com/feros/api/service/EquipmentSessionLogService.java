package com.feros.api.service;

import com.feros.api.dto.request.EquipmentSessionLogCloseRequest;
import com.feros.api.dto.request.EquipmentSessionLogStartRequest;
import com.feros.api.dto.response.EquipmentSessionLogResponse;
import com.feros.api.dto.response.OperatorTodayResponse;

import java.time.LocalDate;
import java.util.List;

public interface EquipmentSessionLogService {

    OperatorTodayResponse getToday();

    List<EquipmentSessionLogResponse> getMy(LocalDate date);

    EquipmentSessionLogResponse start(EquipmentSessionLogStartRequest request);

    EquipmentSessionLogResponse close(Long id, EquipmentSessionLogCloseRequest request);

    void delete(Long id);
}
