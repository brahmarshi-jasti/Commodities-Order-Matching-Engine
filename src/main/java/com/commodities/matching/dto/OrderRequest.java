package com.commodities.matching.dto;

import com.commodities.matching.model.Commodity;
import com.commodities.matching.model.OrderSide;
import com.commodities.matching.model.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderRequest {
    
    @NotNull(message = "Commodity is required")
    private Commodity commodity;
    
    @NotNull(message = "Order side is required")
    private OrderSide side;
    
    @NotNull(message = "Order type is required")
    private OrderType type;
    
    @Positive(message = "Price must be positive")
    private double price;
    
    @Positive(message = "Quantity must be positive")
    private long quantity;
}
