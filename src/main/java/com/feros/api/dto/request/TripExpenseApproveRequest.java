package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripExpenseApproveRequest {
    private List<TripExpenseItemApproveRequest> items;
}
