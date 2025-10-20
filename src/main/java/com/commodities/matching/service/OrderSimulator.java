package com.commodities.matching.service;

import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class OrderSimulator {
    private final MatchingEngine matchingEngine;
    private final Random random = new Random();
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    
    private final double[] basePrices = {
        75.0,
        2000.0,
        25.0,
        4.0,
        3.5
    };

    public OrderSimulator(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @Scheduled(fixedRate = 100)
    public void generateOrders() {
        if (!enabled.get()) return;

        int orderCount = random.nextInt(3) + 1;
        
        for (int i = 0; i < orderCount; i++) {
            Commodity commodity = Commodity.values()[random.nextInt(Commodity.values().length)];
            OrderSide side = random.nextBoolean() ? OrderSide.BUY : OrderSide.SELL;
            OrderType type = random.nextDouble() < 0.9 ? OrderType.LIMIT : OrderType.MARKET;
            
            double basePrice = basePrices[commodity.ordinal()];
            double priceVariation = basePrice * 0.02;
            double price = basePrice + (random.nextDouble() - 0.5) * priceVariation;
            price = Math.round(price * 100.0) / 100.0;
            
            long quantity = (random.nextInt(20) + 1) * 100;
            
            Order order = new Order(commodity, side, type, price, quantity);
            matchingEngine.submitOrder(order);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public boolean isEnabled() {
        return enabled.get();
    }
}
