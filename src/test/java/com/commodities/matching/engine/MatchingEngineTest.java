package com.commodities.matching.engine;

import com.commodities.matching.metrics.MetricsCollector;
import com.commodities.matching.model.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {
    
    private MatchingEngine matchingEngine;
    private MetricsCollector metricsCollector;
    
    @BeforeEach
    void setUp() {
        metricsCollector = new MetricsCollector(new SimpleMeterRegistry());
        matchingEngine = new MatchingEngine(metricsCollector);
        matchingEngine.init();
    }
    
    @Test
    @DisplayName("Should initialize with all commodity order books")
    void shouldInitializeWithAllCommodities() {
        assertThat(matchingEngine.getAllOrderBooks()).hasSize(Commodity.values().length);
        for (Commodity commodity : Commodity.values()) {
            assertThat(matchingEngine.getOrderBook(commodity)).isNotNull();
        }
    }
    
    @Test
    @DisplayName("Should submit order successfully")
    void shouldSubmitOrder() {
        Order order = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        
        assertDoesNotThrow(() -> matchingEngine.submitOrder(order));
    }
    
    @Test
    @DisplayName("Should throw exception when submitting null order")
    void shouldThrowExceptionForNullOrder() {
        assertThatThrownBy(() -> matchingEngine.submitOrder(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order cannot be null");
    }
    
    @Test
    @DisplayName("Should match limit buy and sell orders")
    void shouldMatchLimitOrders() throws InterruptedException {
        AtomicInteger tradeCount = new AtomicInteger(0);
        
        matchingEngine.addTradeListener(trade -> {
            tradeCount.incrementAndGet();
        });
        
        // Submit sell order
        Order sellOrder = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1800.0, 10);
        matchingEngine.submitOrder(sellOrder);
        
        // Give time for processing
        Thread.sleep(100);
        
        // Submit matching buy order
        Order buyOrder = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        matchingEngine.submitOrder(buyOrder);
        
        // Wait for matching
        Thread.sleep(100);
        
        assertThat(tradeCount.get()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should execute market order against best available price")
    void shouldExecuteMarketOrder() throws InterruptedException {
        AtomicInteger tradeCount = new AtomicInteger(0);
        
        matchingEngine.addTradeListener(trade -> {
            tradeCount.incrementAndGet();
        });
        
        // Add limit sell orders at different prices
        matchingEngine.submitOrder(new Order(Commodity.OIL, OrderSide.SELL, OrderType.LIMIT, 80.0, 100));
        matchingEngine.submitOrder(new Order(Commodity.OIL, OrderSide.SELL, OrderType.LIMIT, 81.0, 100));
        
        Thread.sleep(100);
        
        // Submit market buy order
        Order marketBuy = new Order(Commodity.OIL, OrderSide.BUY, OrderType.MARKET, 0.0, 50);
        matchingEngine.submitOrder(marketBuy);
        
        Thread.sleep(100);
        
        assertThat(tradeCount.get()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should partially fill orders when quantities don't match")
    void shouldPartiallyFillOrders() throws InterruptedException {
        AtomicInteger tradeCount = new AtomicInteger(0);
        
        matchingEngine.addTradeListener(trade -> {
            tradeCount.incrementAndGet();
        });
        
        // Sell order with quantity 10
        Order sellOrder = new Order(Commodity.SILVER, OrderSide.SELL, OrderType.LIMIT, 25.0, 10);
        matchingEngine.submitOrder(sellOrder);
        
        Thread.sleep(100);
        
        // Buy order with quantity 5 (partial fill)
        Order buyOrder = new Order(Commodity.SILVER, OrderSide.BUY, OrderType.LIMIT, 25.0, 5);
        matchingEngine.submitOrder(buyOrder);
        
        Thread.sleep(100);
        
        assertThat(tradeCount.get()).isGreaterThan(0);
        assertThat(buyOrder.isFilled()).isTrue();
        assertThat(sellOrder.getRemainingQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should maintain order book depth")
    void shouldMaintainOrderBookDepth() throws InterruptedException {
        // Submit multiple orders
        for (int i = 0; i < 5; i++) {
            matchingEngine.submitOrder(new Order(Commodity.COPPER, OrderSide.BUY, OrderType.LIMIT, 4.0 + i * 0.1, 10));
            matchingEngine.submitOrder(new Order(Commodity.COPPER, OrderSide.SELL, OrderType.LIMIT, 4.5 + i * 0.1, 10));
        }
        
        Thread.sleep(100);
        
        OrderBook orderBook = matchingEngine.getOrderBook(Commodity.COPPER);
        assertThat(orderBook.getBuyOrderCount()).isGreaterThan(0);
        assertThat(orderBook.getSellOrderCount()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should trigger order listener when order is submitted")
    void shouldTriggerOrderListener() throws InterruptedException {
        AtomicInteger orderCount = new AtomicInteger(0);
        
        matchingEngine.addOrderListener(order -> {
            orderCount.incrementAndGet();
        });
        
        Order order = new Order(Commodity.NATURAL_GAS, OrderSide.BUY, OrderType.LIMIT, 3.5, 100);
        matchingEngine.submitOrder(order);
        
        Thread.sleep(100);
        
        assertThat(orderCount.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should handle high throughput orders")
    void shouldHandleHighThroughput() throws InterruptedException {
        int orderCount = 1000;
        
        for (int i = 0; i < orderCount; i++) {
            OrderSide side = i % 2 == 0 ? OrderSide.BUY : OrderSide.SELL;
            double price = 100.0 + (i % 10) * 0.1;
            matchingEngine.submitOrder(new Order(Commodity.OIL, side, OrderType.LIMIT, price, 10));
        }
        
        // Allow processing time
        Thread.sleep(500);
        
        // Verify system is still responsive
        assertDoesNotThrow(() -> {
            Order testOrder = new Order(Commodity.OIL, OrderSide.BUY, OrderType.LIMIT, 100.0, 10);
            matchingEngine.submitOrder(testOrder);
        });
    }
}
