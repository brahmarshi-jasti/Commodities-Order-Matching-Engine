package com.commodities.matching.model;

public enum Commodity {
    CRUDE_OIL("Crude Oil", "OIL"),
    GOLD("Gold", "GOLD"),
    SILVER("Silver", "SILVER"),
    COPPER("Copper", "COPPER"),
    NATURAL_GAS("Natural Gas", "GAS");

    private final String displayName;
    private final String symbol;

    Commodity(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
