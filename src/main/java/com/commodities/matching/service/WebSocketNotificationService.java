package com.commodities.matching.service;

import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.model.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchingEngine matchingEngine;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate, 
                                       MatchingEngine matchingEngine) {
        this.messagingTemplate = messagingTemplate;
        this.matchingEngine = matchingEngine;
    }

    @PostConstruct
    public void init() {
        matchingEngine.addTradeListener(this::broadcastTrade);
        matchingEngine.addOrderListener(this::broadcastOrder);
    }

    private void broadcastTrade(Trade trade) {
        Map<String, Object> tradeData = Map.of(
            "tradeId", trade.getTradeId(),
            "commodity", trade.getCommodity().getSymbol(),
            "price", trade.getPrice(),
            "quantity", trade.getQuantity(),
            "buyOrderId", trade.getBuyOrderId(),
            "sellOrderId", trade.getSellOrderId(),
            "latencyMicros", trade.getProcessingTimeMicros(),
            "timestamp", trade.getTimestamp().toEpochMilli()
        );
        
        messagingTemplate.convertAndSend("/topic/trades", tradeData);
        messagingTemplate.convertAndSend("/topic/trades/" + trade.getCommodity().getSymbol(), tradeData);
    }

    private void broadcastOrder(Order order) {
        Map<String, Object> orderData = Map.of(
            "orderId", order.getOrderId(),
            "commodity", order.getCommodity().getSymbol(),
            "side", order.getSide().toString(),
            "type", order.getType().toString(),
            "price", order.getPrice(),
            "quantity", order.getQuantity(),
            "timestamp", order.getTimestamp().toEpochMilli()
        );
        
        messagingTemplate.convertAndSend("/topic/orders", orderData);
        messagingTemplate.convertAndSend("/topic/orders/" + order.getCommodity().getSymbol(), orderData);
    }
}
