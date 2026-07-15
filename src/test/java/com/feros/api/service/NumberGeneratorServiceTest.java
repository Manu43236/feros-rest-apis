package com.feros.api.service;

import com.feros.api.entity.DocumentSequence;
import com.feros.api.repository.DocumentSequenceRepository;
import com.feros.api.service.impl.NumberGeneratorServiceImpl;
import com.feros.api.util.NumberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumberGeneratorServiceTest {

    @Mock
    private DocumentSequenceRepository sequenceRepo;

    private NumberGeneratorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NumberGeneratorServiceImpl(sequenceRepo);
    }

    @Test
    @DisplayName("First invoice gets sequence 01 (minimum 2 digits)")
    void firstInvoiceIsMinTwoDigits() {
        when(sequenceRepo.findForUpdate(eq(21L), eq("INV"), anyString()))
                .thenReturn(Optional.empty());
        when(sequenceRepo.save(any(DocumentSequence.class)))
                .thenAnswer(inv -> {
                    DocumentSequence s = inv.getArgument(0);
                    return s;
                });

        String number = service.generateFY(21L, NumberUtil.Type.INV);

        // ends with "01" for first sequence
        assertThat(number).startsWith("INV21");
        assertThat(number).endsWith("01");
    }

    @Test
    @DisplayName("10th invoice has no leading zero padding")
    void tenthInvoiceNoLeadingZero() {
        DocumentSequence existing = DocumentSequence.builder()
                .tenantId(21L).docType("INV").period("2627").lastSeq(9L).build();
        when(sequenceRepo.findForUpdate(eq(21L), eq("INV"), anyString()))
                .thenReturn(Optional.of(existing));
        when(sequenceRepo.save(any(DocumentSequence.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String number = service.generateFY(21L, NumberUtil.Type.INV);

        assertThat(number).endsWith("10");
    }

    @Test
    @DisplayName("Master (CLNT) has no FY period in number")
    void masterNumberHasNoFY() {
        when(sequenceRepo.findForUpdate(eq(21L), eq("CLNT"), eq("MASTER")))
                .thenReturn(Optional.empty());
        when(sequenceRepo.save(any(DocumentSequence.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String number = service.generateSequential(21L, NumberUtil.Type.CLNT);

        assertThat(number).startsWith("CLNT21");
        assertThat(number).endsWith("01");
        // No 4-digit FY between CLNT21 and 01
        assertThat(number).isEqualTo("CLNT21" + "01");
    }

    @Test
    @DisplayName("Monthly (PR) number includes MMYY period")
    void monthlyNumberIncludesMonthYear() {
        when(sequenceRepo.findForUpdate(eq(21L), eq("PR"), anyString()))
                .thenReturn(Optional.empty());
        when(sequenceRepo.save(any(DocumentSequence.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String number = service.generateMonthly(21L, NumberUtil.Type.PR);

        assertThat(number).startsWith("PR21");
        // period is MMYY (4 chars) + seq (min 2 chars) = at least 6 chars after "PR21"
        assertThat(number.length()).isGreaterThanOrEqualTo("PR21".length() + 6);
    }
}
