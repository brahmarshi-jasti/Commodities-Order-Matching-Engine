package com.commodities.matching.engine;

import com.commodities.matching.model.*;
import com.commodities.matching.metrics.MetricsCollector;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
public class MatchingEngine {
    private static final Logger logger = LoggerFactory.getLogger(MatchingEngine.class);
    private static final int RING_BUFFER_SIZE = 1024 * 64;
    private final Map<Commodity, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final AtomicLong tradeIdGenerator = new AtomicLong(1);
    private final MetricsCollector metricsCollector;
    
    private Disruptor<OrderEvent> disruptor;
    private RingBuffer<OrderEvent> ringBuffer;
    private final List<Consumer<Trade>> tradeListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Order>> orderListeners = new CopyOnWriteArrayList<>();

    public MatchingEngine(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        for (Commodity commodity : Commodity.values()) {
            orderBooks.put(commodity, new OrderBook(commodity));
        }
        logger.info("MatchingEngine initialized with {} commodity order books", Commodity.values().length);
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing LMAX Disruptor with ring buffer size: {}", RING_BUFFER_SIZE);
            ThreadFactory threadFactory = r -> {
                Thread t = new Thread(r);
                t.setName("matching-engine");
                t.setDaemon(true);
                return t;
            };

            disruptor = new Disruptor<>(
                OrderEvent::new,
                RING_BUFFER_SIZE,
                threadFactory,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
            );

            disruptor.handleEventsWith(this::handleOrderEvent);
            disruptor.start();
            ringBuffer = disruptor.getRingBuffer();
            logger.info("MatchingEngine started successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MatchingEngine", e);
            throw new RuntimeException("MatchingEngine initialization failed", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            logger.info("Shutting down MatchingEngine...");
            if (disruptor != null) {
                disruptor.shutdown();
                logger.info("MatchingEngine shutdown completed");
            }
        } catch (Exception e) {
            logger.error("Error during MatchingEngine shutdown", e);
        }
    }

    public void submitOrder(Order order) {
        if (order == null) {
            logger.warn("Attempted to submit null order");
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.order = order;
            event.submissionTime = System.nanoTime();
            logger.debug("Order submitted: {} {} {} @ {} qty:{}", 
                order.getOrderId(), order.getSide(), order.getCommodity(), 
                order.getPrice(), order.getQuantity());
        } catch (Exception e) {
            logger.error("Error submitting order: {}", order.getOrderId(), e);
            throw e;
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void handleOrderEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        long startTime = System.nanoTime();
        Order order = event.order;
        
        try {
            metricsCollector.recordOrderReceived(order.getCommodity());
            notifyOrderListeners(order);

            OrderBook orderBook = orderBooks.get(order.getCommodity());
            
            if (order.getType() == OrderType.MARKET) {
                matchMarketOrder(order, orderBook, startTime);
            } else {
                matchLimitOrder(order, orderBook, startTime);
            }

            long processingTime = System.nanoTime() - startTime;
            metricsCollector.recordOrderProcessingTime(processingTime);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Order {} processed in {} ns", order.getOrderId(), processingTime);
            }
        } catch (Exception e) {
            logger.error("Error processing order: {}", order.getOrderId(), e);
        }
    }

    private void matchMarketOrder(Order order, OrderBook orderBook, long startTime) {
        Comparator<Order> comparator = order.getSide() == OrderSide.BUY 
            ? Comparator.comparingDouble(Order::getPrice)
            : Comparator.comparingDouble(Order::getPrice).reversed();
        
        List<Order> orders = order.getSide() == OrderSide.BUY 
            ? orderBook.getSellOrders(100)
            : orderBook.getBuyOrders(100);
        
        PriorityQueue<Order> oppositeBook = new PriorityQueue<>(comparator);
        oppositeBook.addAll(orders);

        while (!oppositeBook.isEmpty() && order.getRemainingQuantity() > 0) {
            Order counterOrder = oppositeBook.poll();
            if (counterOrder.isFilled()) continue;

            executeTrade(order, counterOrder, counterOrder.getPrice(), startTime);
        }

        if (order.getRemainingQuantity() > 0) {
            metricsCollector.recordPartialFill(order.getCommodity());
        } else {
            metricsCollector.recordCompleteFill(order.getCommodity());
        }
    }

    private void matchLimitOrder(Order order, OrderBook orderBook, long startTime) {
        boolean matched = false;

        if (order.getSide() == OrderSide.BUY) {
            List<Order> sellOrders = new ArrayList<>(orderBook.getSellOrders(100));
            for (Order sellOrder : sellOrders) {
                if (sellOrder.isFilled()) continue;
                if (order.getPrice() >= sellOrder.getPrice() && order.getRemainingQuantity() > 0) {
                    executeTrade(order, sellOrder, sellOrder.getPrice(), startTime);
                    matched = true;
                } else {
                    break;
                }
            }
        } else {
            List<Order> buyOrders = new ArrayList<>(orderBook.getBuyOrders(100));
            for (Order buyOrder : buyOrders) {
                if (buyOrder.isFilled()) continue;
                if (order.getPrice() <= buyOrder.getPrice() && order.getRemainingQuantity() > 0) {
                    executeTrade(order, buyOrder, buyOrder.getPrice(), startTime);
                    matched = true;
                } else {
                    break;
                }
            }
        }

        if (order.getRemainingQuantity() > 0) {
            orderBook.addOrder(order);
            if (!matched) {
                metricsCollector.recordOrderAdded(order.getCommodity());
            } else {
                metricsCollector.recordPartialFill(order.getCommodity());
            }
        } else {
            metricsCollector.recordCompleteFill(order.getCommodity());
        }
    }

    private void executeTrade(Order aggressiveOrder, Order passiveOrder, double tradePrice, long startTime) {
        long tradeQuantity = Math.min(aggressiveOrder.getRemainingQuantity(), passiveOrder.getRemainingQuantity());
        
        aggressiveOrder.setRemainingQuantity(aggressiveOrder.getRemainingQuantity() - tradeQuantity);
        passiveOrder.setRemainingQuantity(passiveOrder.getRemainingQuantity() - tradeQuantity);

        OrderBook orderBook = orderBooks.get(aggressiveOrder.getCommodity());
        
        if (passiveOrder.isFilled()) {
            orderBook.removeOrder(passiveOrder.getOrderId());
        }

        long processingTime = System.nanoTime() - startTime;
        
        Trade trade = new Trade(
            tradeIdGenerator.getAndIncrement(),
            aggressiveOrder.getSide() == OrderSide.BUY ? aggressiveOrder.getOrderId() : passiveOrder.getOrderId(),
            aggressiveOrder.getSide() == OrderSide.SELL ? aggressiveOrder.getOrderId() : passiveOrder.getOrderId(),
            aggressiveOrder.getCommodity(),
            tradePrice,
            tradeQuantity,
            processingTime
        );

        double slippage = Math.abs(aggressiveOrder.getPrice() - tradePrice);
        metricsCollector.recordTrade(trade, slippage);
        notifyTradeListeners(trade);
        
        logger.debug("Trade executed: {} - {} @ {} qty:{} (slippage: {})", 
            trade.getTradeId(), aggressiveOrder.getCommodity(), tradePrice, tradeQuantity, slippage);
    }

    public void addTradeListener(Consumer<Trade> listener) {
        tradeListeners.add(listener);
    }

    public void addOrderListener(Consumer<Order> listener) {
        orderListeners.add(listener);
    }

    private void notifyTradeListeners(Trade trade) {
        for (Consumer<Trade> listener : tradeListeners) {
            try {
                listener.accept(trade);
            } catch (Exception e) {
                logger.error("Error notifying trade listener for trade: {}", trade.getTradeId(), e);
            }
        }
    }

    private void notifyOrderListeners(Order order) {
        for (Consumer<Order> listener : orderListeners) {
            try {
                listener.accept(order);
            } catch (Exception e) {
                logger.error("Error notifying order listener for order: {}", order.getOrderId(), e);
            }
        }
    }

    public OrderBook getOrderBook(Commodity commodity) {
        return orderBooks.get(commodity);
    }

    public Map<Commodity, OrderBook> getAllOrderBooks() {
        return new HashMap<>(orderBooks);
    }

    static class OrderEvent {
        Order order;
        long submissionTime;
    }
}
