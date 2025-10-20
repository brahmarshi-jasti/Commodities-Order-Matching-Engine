package com.commodities.matching.metrics;

import com.commodities.matching.model.Commodity;
import com.commodities.matching.model.Trade;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Component
public class MetricsCollector {
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<Commodity, AtomicLong> ordersReceived = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Commodity, AtomicLong> tradesExecuted = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Commodity, AtomicLong> completeFills = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Commodity, AtomicLong> partialFills = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Commodity, DoubleAdder> totalSlippage = new ConcurrentHashMap<>();
    private final AtomicLong totalOrders = new AtomicLong(0);
    private final AtomicLong totalTrades = new AtomicLong(0);
    private final DoubleAdder avgLatencyNanos = new DoubleAdder();
    private final AtomicLong latencyCount = new AtomicLong(0);

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        for (Commodity commodity : Commodity.values()) {
            ordersReceived.put(commodity, new AtomicLong(0));
            tradesExecuted.put(commodity, new AtomicLong(0));
            completeFills.put(commodity, new AtomicLong(0));
            partialFills.put(commodity, new AtomicLong(0));
            totalSlippage.put(commodity, new DoubleAdder());
        }

        Gauge.builder("matching.engine.total.orders", totalOrders, AtomicLong::get)
            .description("Total orders received")
            .register(meterRegistry);

        Gauge.builder("matching.engine.total.trades", totalTrades, AtomicLong::get)
            .description("Total trades executed")
            .register(meterRegistry);

        Gauge.builder("matching.engine.avg.latency.micros", this, MetricsCollector::getAvgLatencyMicros)
            .description("Average order processing latency in microseconds")
            .register(meterRegistry);
    }

    public void recordOrderReceived(Commodity commodity) {
        ordersReceived.get(commodity).incrementAndGet();
        totalOrders.incrementAndGet();
        meterRegistry.counter("matching.engine.orders.received", "commodity", commodity.getSymbol()).increment();
    }

    public void recordOrderAdded(Commodity commodity) {
        meterRegistry.counter("matching.engine.orders.added", "commodity", commodity.getSymbol()).increment();
    }

    public void recordTrade(Trade trade, double slippage) {
        tradesExecuted.get(trade.getCommodity()).incrementAndGet();
        totalTrades.incrementAndGet();
        totalSlippage.get(trade.getCommodity()).add(slippage);
        
        meterRegistry.counter("matching.engine.trades.executed", "commodity", trade.getCommodity().getSymbol()).increment();
        meterRegistry.summary("matching.engine.trade.latency.nanos", "commodity", trade.getCommodity().getSymbol())
            .record(trade.getProcessingTimeNanos());
    }

    public void recordCompleteFill(Commodity commodity) {
        completeFills.get(commodity).incrementAndGet();
        meterRegistry.counter("matching.engine.fills.complete", "commodity", commodity.getSymbol()).increment();
    }

    public void recordPartialFill(Commodity commodity) {
        partialFills.get(commodity).incrementAndGet();
        meterRegistry.counter("matching.engine.fills.partial", "commodity", commodity.getSymbol()).increment();
    }

    public void recordOrderProcessingTime(long nanos) {
        avgLatencyNanos.add(nanos);
        latencyCount.incrementAndGet();
        meterRegistry.summary("matching.engine.processing.time.nanos").record(nanos);
    }

    public double getAvgLatencyMicros() {
        long count = latencyCount.get();
        if (count == 0) return 0;
        return (avgLatencyNanos.sum() / count) / 1000.0;
    }

    public EngineMetrics getMetrics() {
        EngineMetrics metrics = new EngineMetrics();
        metrics.totalOrders = totalOrders.get();
        metrics.totalTrades = totalTrades.get();
        metrics.avgLatencyMicros = getAvgLatencyMicros();
        
        for (Commodity commodity : Commodity.values()) {
            CommodityMetrics cm = new CommodityMetrics();
            cm.commodity = commodity.getSymbol();
            cm.ordersReceived = ordersReceived.get(commodity).get();
            cm.tradesExecuted = tradesExecuted.get(commodity).get();
            cm.completeFills = completeFills.get(commodity).get();
            cm.partialFills = partialFills.get(commodity).get();
            
            long totalFills = cm.completeFills + cm.partialFills;
            cm.fillRate = totalFills > 0 ? (double) cm.completeFills / totalFills * 100.0 : 0.0;
            
            long trades = tradesExecuted.get(commodity).get();
            cm.avgSlippage = trades > 0 ? totalSlippage.get(commodity).sum() / trades : 0.0;
            
            metrics.commodities.put(commodity.getSymbol(), cm);
        }
        
        return metrics;
    }

    public static class EngineMetrics {
        public long totalOrders;
        public long totalTrades;
        public double avgLatencyMicros;
        public ConcurrentHashMap<String, CommodityMetrics> commodities = new ConcurrentHashMap<>();
    }

    public static class CommodityMetrics {
        public String commodity;
        public long ordersReceived;
        public long tradesExecuted;
        public long completeFills;
        public long partialFills;
        public double fillRate;
        public double avgSlippage;
    }
}
