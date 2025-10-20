package com.commodities.matching.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_commodity", columnList = "commodity"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
public class TradeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tradeId;
    
    @Column(nullable = false)
    private Long buyOrderId;
    
    @Column(nullable = false)
    private Long sellOrderId;
    
    @Column(nullable = false)
    private String commodity;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Long quantity;
    
    @Column(nullable = false)
    private Long processingTimeNanos;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    public TradeEntity(Long tradeId, Long buyOrderId, Long sellOrderId, 
                      String commodity, Double price, Long quantity, 
                      Long processingTimeNanos) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.commodity = commodity;
        this.price = price;
        this.quantity = quantity;
        this.processingTimeNanos = processingTimeNanos;
        this.timestamp = Instant.now();
    }
}
