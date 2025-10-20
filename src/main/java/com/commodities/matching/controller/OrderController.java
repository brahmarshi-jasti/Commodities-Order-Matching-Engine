package com.commodities.matching.controller;

import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final MatchingEngine matchingEngine;

    public OrderController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitOrder(@RequestBody OrderRequest request) {
        Order order = new Order(
            request.commodity,
            request.side,
            request.type,
            request.price,
            request.quantity
        );

        matchingEngine.submitOrder(order);

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

    public static class OrderRequest {
        public Commodity commodity;
        public OrderSide side;
        public OrderType type;
        public double price;
        public long quantity;
    }
}
