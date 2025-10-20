package com.commodities.matching.controller;

import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.engine.OrderBook;
import com.commodities.matching.model.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/orderbook")
public class OrderBookController {
    private final MatchingEngine matchingEngine;

    public OrderBookController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @GetMapping("/{commodity}")
    public Map<String, Object> getOrderBook(@PathVariable Commodity commodity, 
                                            @RequestParam(defaultValue = "10") int depth) {
        OrderBook orderBook = matchingEngine.getOrderBook(commodity);
        
        List<Map<String, Object>> bids = orderBook.getBuyOrders(depth).stream()
            .map(this::orderToMap)
            .toList();
            
        List<Map<String, Object>> asks = orderBook.getSellOrders(depth).stream()
            .map(this::orderToMap)
            .toList();

        Order bestBid = orderBook.getBestBid();
        Order bestAsk = orderBook.getBestAsk();
        
        double spread = (bestBid != null && bestAsk != null) 
            ? bestAsk.getPrice() - bestBid.getPrice() 
            : 0.0;

        return Map.of(
            "commodity", commodity.getSymbol(),
            "bids", bids,
            "asks", asks,
            "bestBid", bestBid != null ? bestBid.getPrice() : 0.0,
            "bestAsk", bestAsk != null ? bestAsk.getPrice() : 0.0,
            "spread", spread,
            "bidCount", orderBook.getBuyOrderCount(),
            "askCount", orderBook.getSellOrderCount()
        );
    }

    @GetMapping
    public Map<String, Map<String, Object>> getAllOrderBooks() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        for (Commodity commodity : Commodity.values()) {
            result.put(commodity.getSymbol(), getOrderBook(commodity, 5));
        }
        
        return result;
    }

    private Map<String, Object> orderToMap(Order order) {
        return Map.of(
            "orderId", order.getOrderId(),
            "price", order.getPrice(),
            "quantity", order.getRemainingQuantity(),
            "side", order.getSide().toString()
        );
    }
}
