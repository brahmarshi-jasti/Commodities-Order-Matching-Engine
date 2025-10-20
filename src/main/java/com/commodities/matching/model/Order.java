package com.commodities.matching.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Order {
    private static final AtomicLong ORDER_ID_GENERATOR = new AtomicLong(1);

    private final long orderId;
    private final Commodity commodity;
    private final OrderSide side;
    private final OrderType type;
    private final double price;
    private final long quantity;
    private long remainingQuantity;
    private final Instant timestamp;
    private final long nanoTime;

    public Order(Commodity commodity, OrderSide side, OrderType type, double price, long quantity) {
        this.orderId = ORDER_ID_GENERATOR.getAndIncrement();
        this.commodity = commodity;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.timestamp = Instant.now();
        this.nanoTime = System.nanoTime();
    }

    public long getOrderId() {
        return orderId;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(long remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }
}
