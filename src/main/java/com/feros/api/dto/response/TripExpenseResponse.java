package com.feros.api.dto.response;

import com.feros.api.enums.PaymentMode;
import com.feros.api.enums.TripExpenseStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class TripExpenseResponse {
    private Long id;
    private Long lrId;
    private String lrNumber;
    private String driverName;
    private String cleanerName;

    private BigDecimal advanceAmount;
    private Integer tripDays;
    private BigDecimal driverBatta;
    private BigDecimal cleanerBatta;
    private BigDecimal tripMamulu;

    // calculated totals
    private BigDecimal totalFixedAmount;        // driverBatta + cleanerBatta + tripMamulu
    private BigDecimal totalOperationalAmount;  // sum of item amounts
    private BigDecimal totalSubmittedAmount;    // totalFixed + totalOperational
    private BigDecimal totalApprovedAmount;     // totalFixed + sum of approvedAmounts
    private BigDecimal balanceAmount;           // advance - totalApproved (positive = driver returns, negative = company pays)

    private TripExpenseStatus status;
    private String submittedByName;
    private LocalDateTime submittedAt;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private BigDecimal settlementAmount;
    private String settlementNote;
    private PaymentMode paymentMode;
    private String settledByName;
    private LocalDateTime settledAt;

    private String driverPhone;
    private String driverBankName;
    private String driverAccountNumber;
    private String driverIfscCode;
    private String rejectedByName;
    private LocalDateTime rejectedAt;
    private String rejectionReason;

    private List<TripExpenseItemResponse> items;
    private LocalDateTime createdAt;
}
