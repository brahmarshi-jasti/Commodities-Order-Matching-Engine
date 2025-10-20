package com.commodities.matching.model;

import java.time.Instant;

public class Trade {
    private final long tradeId;
    private final long buyOrderId;
    private final long sellOrderId;
    private final Commodity commodity;
    private final double price;
    private final long quantity;
    private final Instant timestamp;
    private final long processingTimeNanos;

    public Trade(long tradeId, long buyOrderId, long sellOrderId, Commodity commodity, 
                 double price, long quantity, long processingTimeNanos) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.commodity = commodity;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
        this.processingTimeNanos = processingTimeNanos;
    }

    public long getTradeId() {
        return tradeId;
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getProcessingTimeNanos() {
        return processingTimeNanos;
    }

    public double getProcessingTimeMicros() {
        return processingTimeNanos / 1000.0;
    }
}
