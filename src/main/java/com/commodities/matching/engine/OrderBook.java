package com.commodities.matching.engine;

import com.commodities.matching.model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderBook {
    private final Commodity commodity;
    private final PriorityBlockingQueue<Order> buyOrders;
    private final PriorityBlockingQueue<Order> sellOrders;
    private final Map<Long, Order> orderMap;

    public OrderBook(Commodity commodity) {
        this.commodity = commodity;
        this.buyOrders = new PriorityBlockingQueue<>(1000, (o1, o2) -> {
            int priceCompare = Double.compare(o2.getPrice(), o1.getPrice());
            if (priceCompare != 0) return priceCompare;
            return Long.compare(o1.getNanoTime(), o2.getNanoTime());
        });
        
        this.sellOrders = new PriorityBlockingQueue<>(1000, (o1, o2) -> {
            int priceCompare = Double.compare(o1.getPrice(), o2.getPrice());
            if (priceCompare != 0) return priceCompare;
            return Long.compare(o1.getNanoTime(), o2.getNanoTime());
        });
        
        this.orderMap = new ConcurrentHashMap<>();
    }

    public void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
        if (order.getSide() == OrderSide.BUY) {
            buyOrders.offer(order);
        } else {
            sellOrders.offer(order);
        }
    }

    public void removeOrder(long orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            if (order.getSide() == OrderSide.BUY) {
                buyOrders.remove(order);
            } else {
                sellOrders.remove(order);
            }
        }
    }

    public Order getBestBid() {
        return buyOrders.peek();
    }

    public Order getBestAsk() {
        return sellOrders.peek();
    }

    public List<Order> getBuyOrders(int limit) {
        return new ArrayList<>(buyOrders).stream()
            .filter(o -> !o.isFilled())
            .limit(limit)
            .toList();
    }

    public List<Order> getSellOrders(int limit) {
        return new ArrayList<>(sellOrders).stream()
            .filter(o -> !o.isFilled())
            .limit(limit)
            .toList();
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public int getBuyOrderCount() {
        return (int) buyOrders.stream().filter(o -> !o.isFilled()).count();
    }

    public int getSellOrderCount() {
        return (int) sellOrders.stream().filter(o -> !o.isFilled()).count();
    }
}
