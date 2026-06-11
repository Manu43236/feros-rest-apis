package com.feros.api.service;

import com.feros.api.entity.Lr;
import com.feros.api.entity.Order;
import com.feros.api.entity.master.Route;
import com.feros.api.enums.BillingOn;
import com.feros.api.enums.FreightRateType;
import com.feros.api.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the private {@code calculateFreightAmount} method in
 * {@link InvoiceServiceImpl}.  We use reflection to call it directly so
 * we can test every billing/rate combination without setting up the full
 * service dependency graph.
 */
class InvoiceFreightCalculationTest {

    // ─── Freight rate types ───────────────────────────────────────────────────

    @Test
    @DisplayName("PER_TON: freightAmount = freightRate × loadedWeight")
    void calculateFreightAmount_perTon_usesLoadedWeight() throws Exception {
        Order order = buildOrder(FreightRateType.PER_TON, BillingOn.LOADED_WEIGHT,
            new BigDecimal("150.00"), null);
        Lr lr = buildLr(new BigDecimal("10.00"), null, null);

        BigDecimal result = invokeCalculate(lr, order);

        // 150 × 10 = 1500
        assertThat(result).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("PER_TRIP: freightAmount = freightRate (fixed, ignores weight)")
    void calculateFreightAmount_perTrip_returnsFixedRate() throws Exception {
        Order order = buildOrder(FreightRateType.PER_TRIP, BillingOn.LOADED_WEIGHT,
            new BigDecimal("5000.00"), null);
        Lr lr = buildLr(new BigDecimal("10.00"), null, null);

        BigDecimal result = invokeCalculate(lr, order);

        assertThat(result).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("PER_KM with route: freightAmount = freightRate × distanceKm")
    void calculateFreightAmount_perKmWithRoute_usesDistance() throws Exception {
        Route route = new Route();
        route.setDistanceInKm(new BigDecimal("250.00"));

        Order order = buildOrder(FreightRateType.PER_KM, BillingOn.LOADED_WEIGHT,
            new BigDecimal("20.00"), route);
        Lr lr = buildLr(new BigDecimal("10.00"), null, null);

        BigDecimal result = invokeCalculate(lr, order);

        // 20 × 250 = 5000
        assertThat(result).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("PER_KM without route: falls back to freightRate")
    void calculateFreightAmount_perKmWithoutRoute_returnsFallback() throws Exception {
        Order order = buildOrder(FreightRateType.PER_KM, BillingOn.LOADED_WEIGHT,
            new BigDecimal("20.00"), null);
        Lr lr = buildLr(new BigDecimal("10.00"), null, null);

        BigDecimal result = invokeCalculate(lr, order);

        assertThat(result).isEqualByComparingTo("20.00");
    }

    // ─── Billing basis ────────────────────────────────────────────────────────

    @Test
    @DisplayName("LOADED_WEIGHT billing: uses loadedWeight when set")
    void calculateFreightAmount_billedOnLoaded_usesLoadedWeight() throws Exception {
        Order order = buildOrder(FreightRateType.PER_TON, BillingOn.LOADED_WEIGHT,
            new BigDecimal("100.00"), null);
        // allocated=8, loaded=10, delivered=9
        Lr lr = buildLr(new BigDecimal("10.00"), new BigDecimal("9.00"), new BigDecimal("8.00"));

        BigDecimal result = invokeCalculate(lr, order);

        // billedWeight=10 → 100×10=1000
        assertThat(result).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("DELIVERED_WEIGHT billing: uses deliveredWeight when set")
    void calculateFreightAmount_billedOnDelivered_usesDeliveredWeight() throws Exception {
        Order order = buildOrder(FreightRateType.PER_TON, BillingOn.DELIVERED_WEIGHT,
            new BigDecimal("100.00"), null);
        // allocated=8, loaded=10, delivered=9
        Lr lr = buildLr(new BigDecimal("10.00"), new BigDecimal("9.00"), new BigDecimal("8.00"));

        BigDecimal result = invokeCalculate(lr, order);

        // billedWeight=9 → 100×9=900
        assertThat(result).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("DELIVERED_WEIGHT billing: falls back to loadedWeight when deliveredWeight is null")
    void calculateFreightAmount_billedOnDelivered_nullDelivered_fallsBackToLoaded() throws Exception {
        Order order = buildOrder(FreightRateType.PER_TON, BillingOn.DELIVERED_WEIGHT,
            new BigDecimal("100.00"), null);
        // loaded=10, delivered=null
        Lr lr = buildLr(new BigDecimal("10.00"), null, new BigDecimal("8.00"));

        BigDecimal result = invokeCalculate(lr, order);

        // falls back to loadedWeight=10 → 1000
        assertThat(result).isEqualByComparingTo("1000.00");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal invokeCalculate(Lr lr, Order order) throws Exception {
        // Use reflection to access the private method
        Method m = InvoiceServiceImpl.class
            .getDeclaredMethod("calculateFreightAmount", Lr.class, Order.class);
        m.setAccessible(true);

        // InvoiceServiceImpl has many constructor dependencies — mock them all
        InvoiceServiceImpl service = mock(InvoiceServiceImpl.class, org.mockito.Answers.CALLS_REAL_METHODS);
        return (BigDecimal) m.invoke(service, lr, order);
    }

    private Order buildOrder(FreightRateType rateType, BillingOn billingOn,
                             BigDecimal freightRate, Route route) {
        Order order = new Order();
        order.setFreightRateType(rateType);
        order.setBillingOn(billingOn);
        order.setFreightRate(freightRate);
        order.setRoute(route);
        return order;
    }

    /** loaded=loadedWeight, delivered=deliveredWeight, allocated=allocatedWeight */
    private Lr buildLr(BigDecimal loaded, BigDecimal delivered, BigDecimal allocated) {
        Lr lr = new Lr();
        lr.setLoadedWeight(loaded);
        lr.setDeliveredWeight(delivered);
        lr.setAllocatedWeight(allocated);
        return lr;
    }
}
