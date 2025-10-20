package com.commodities.matching.engine;

import com.commodities.matching.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderBookTest {
    
    private OrderBook orderBook;
    
    @BeforeEach
    void setUp() {
        orderBook = new OrderBook(Commodity.GOLD);
    }
    
    @Test
    @DisplayName("Should initialize with correct commodity")
    void shouldInitializeWithCommodity() {
        assertThat(orderBook.getCommodity()).isEqualTo(Commodity.GOLD);
    }
    
    @Test
    @DisplayName("Should add buy order to order book")
    void shouldAddBuyOrder() {
        Order order = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        orderBook.addOrder(order);
        
        assertThat(orderBook.getBuyOrderCount()).isEqualTo(1);
        assertThat(orderBook.getBestBid()).isEqualTo(order);
    }
    
    @Test
    @DisplayName("Should add sell order to order book")
    void shouldAddSellOrder() {
        Order order = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1800.0, 10);
        orderBook.addOrder(order);
        
        assertThat(orderBook.getSellOrderCount()).isEqualTo(1);
        assertThat(orderBook.getBestAsk()).isEqualTo(order);
    }
    
    @Test
    @DisplayName("Should maintain price-time priority for buy orders")
    void shouldMaintainPriceTimePriorityForBuys() {
        Order order1 = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        Order order2 = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1805.0, 10);
        Order order3 = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1795.0, 10);
        
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        
        // Best bid should be highest price (1805.0)
        assertThat(orderBook.getBestBid()).isEqualTo(order2);
    }
    
    @Test
    @DisplayName("Should maintain price-time priority for sell orders")
    void shouldMaintainPriceTimePriorityForSells() {
        Order order1 = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1800.0, 10);
        Order order2 = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1795.0, 10);
        Order order3 = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1805.0, 10);
        
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        
        // Best ask should be lowest price (1795.0)
        assertThat(orderBook.getBestAsk()).isEqualTo(order2);
    }
    
    @Test
    @DisplayName("Should remove order from order book")
    void shouldRemoveOrder() {
        Order order = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        orderBook.addOrder(order);
        
        assertThat(orderBook.getBuyOrderCount()).isEqualTo(1);
        
        orderBook.removeOrder(order.getOrderId());
        
        assertThat(orderBook.getBuyOrderCount()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should get buy orders up to limit")
    void shouldGetBuyOrdersWithLimit() {
        for (int i = 0; i < 10; i++) {
            Order order = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0 + i, 10);
            orderBook.addOrder(order);
        }
        
        List<Order> orders = orderBook.getBuyOrders(5);
        assertThat(orders).hasSize(5);
    }
    
    @Test
    @DisplayName("Should get sell orders up to limit")
    void shouldGetSellOrdersWithLimit() {
        for (int i = 0; i < 10; i++) {
            Order order = new Order(Commodity.GOLD, OrderSide.SELL, OrderType.LIMIT, 1800.0 + i, 10);
            orderBook.addOrder(order);
        }
        
        List<Order> orders = orderBook.getSellOrders(5);
        assertThat(orders).hasSize(5);
    }
    
    @Test
    @DisplayName("Should exclude filled orders from count")
    void shouldExcludeFilledOrders() {
        Order order1 = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0, 10);
        Order order2 = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1805.0, 10);
        
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        
        // Fill first order
        order1.setRemainingQuantity(0);
        
        List<Order> orders = orderBook.getBuyOrders(10);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0)).isEqualTo(order2);
    }
    
    @Test
    @DisplayName("Should handle concurrent order additions")
    void shouldHandleConcurrentAdditions() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    Order order = new Order(Commodity.GOLD, OrderSide.BUY, OrderType.LIMIT, 1800.0 + index, 10);
                    orderBook.addOrder(order);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertThat(orderBook.getBuyOrderCount()).isGreaterThan(0);
    }
}
