package com.commodities.matching.controller;

import com.commodities.matching.dto.OrderRequest;
import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for submitting and managing orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final MatchingEngine matchingEngine;

    public OrderController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @PostMapping
    @Operation(summary = "Submit a new order", description = "Submit a new buy or sell order to the matching engine")
    public ResponseEntity<Map<String, Object>> submitOrder(@Valid @RequestBody OrderRequest request) {
        logger.info("Received order: {} {} {} @ {} qty:{}", 
            request.getSide(), request.getCommodity(), request.getType(), 
            request.getPrice(), request.getQuantity());
        
        Order order = new Order(
            request.getCommodity(),
            request.getSide(),
            request.getType(),
            request.getPrice(),
            request.getQuantity()
        );

        matchingEngine.submitOrder(order);
        
        logger.info("Order submitted successfully with ID: {}", order.getOrderId());

        return ResponseEntity.ok(Map.of(
            "orderId", order.getOrderId(),
            "status", "submitted",
            "commodity", order.getCommodity().getSymbol(),
            "side", order.getSide(),
            "type", order.getType(),
            "price", order.getPrice(),
            "quantity", order.getQuantity()
        ));
    }
}
