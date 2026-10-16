package com.feros.api.service;

import com.feros.api.dto.request.OrderItemRequest;
import com.feros.api.dto.response.OrderItemResponse;
import com.feros.api.entity.Order;
import com.feros.api.entity.OrderItem;
import com.feros.api.entity.Tenant;
import com.feros.api.entity.master.MaterialType;
import com.feros.api.entity.master.Unit;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.MaterialTypeRepository;
import com.feros.api.repository.OrderItemRepository;
import com.feros.api.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Cargo-manifest (multi-material) concerns, shared by the regular Order flow and the POL flow.
 * Purely additive: everything here is a no-op unless an order opts into multi-material mode.
 */
@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final MaterialTypeRepository materialTypeRepository;
    private final UnitRepository unitRepository;
    private final OrderItemRepository orderItemRepository;

    /** True only when the toggle is on AND at least one manifest line was provided. */
    public boolean isMulti(Boolean flag, List<OrderItemRequest> items) {
        return Boolean.TRUE.equals(flag) && items != null && !items.isEmpty();
    }

    /** Resolve a material from a known ID or a free-text custom name ("Other"), mirroring the order-level rule. */
    public MaterialType resolveMaterial(Long materialTypeId, String customName) {
        if (materialTypeId != null) {
            return materialTypeRepository.findById(materialTypeId)
                    .orElseThrow(() -> new FerosException("Material type not found", HttpStatus.NOT_FOUND));
        }
        if (customName != null && !customName.isBlank()) {
            String name = customName.trim();
            return materialTypeRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> materialTypeRepository.save(
                            MaterialType.builder().name(name).isActive(true).build()));
        }
        throw new FerosException("Material type is required on every line", HttpStatus.BAD_REQUEST);
    }

    /** The order's primary material = the first manifest line (keeps Order.materialType intact). */
    public MaterialType primaryMaterial(List<OrderItemRequest> items) {
        OrderItemRequest first = items.get(0);
        return resolveMaterial(first.getMaterialTypeId(), first.getCustomMaterialName());
    }

    /** Soft-delete any existing lines, then insert the manifest fresh (1-based line numbers). */
    public void replaceItems(Order order, Tenant tenant, List<OrderItemRequest> items) {
        clearItems(order);
        int lineNo = 1;
        List<OrderItem> fresh = new ArrayList<>();
        for (OrderItemRequest r : items) {
            MaterialType material = resolveMaterial(r.getMaterialTypeId(), r.getCustomMaterialName());
            Unit unit = unitRepository.findById(r.getUnitId())
                    .orElseThrow(() -> new FerosException("Unit not found: " + r.getUnitId(), HttpStatus.NOT_FOUND));
            fresh.add(OrderItem.builder()
                    .tenant(tenant)
                    .order(order)
                    .materialType(material)
                    .quantity(r.getQuantity())
                    .unit(unit)
                    .lineNo(lineNo++)
                    .isActive(true)
                    .build());
        }
        orderItemRepository.saveAll(fresh);
    }

    /** Soft-delete all manifest lines for an order (used when an edit toggles multi-material off). */
    public void clearItems(Order order) {
        List<OrderItem> existing = orderItemRepository.findByOrderIdAndIsActiveTrueOrderByLineNoAsc(order.getId());
        if (existing.isEmpty()) return;
        existing.forEach(i -> i.setIsActive(false));
        orderItemRepository.saveAll(existing);
    }

    public List<OrderItemResponse> toResponses(Long orderId) {
        return orderItemRepository.findByOrderIdAndIsActiveTrueOrderByLineNoAsc(orderId).stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .lineNo(i.getLineNo())
                        .materialTypeId(i.getMaterialType().getId())
                        .materialName(i.getMaterialType().getName())
                        .quantity(i.getQuantity())
                        .unitId(i.getUnit().getId())
                        .unitName(i.getUnit().getName())
                        .build())
                .toList();
    }
}
