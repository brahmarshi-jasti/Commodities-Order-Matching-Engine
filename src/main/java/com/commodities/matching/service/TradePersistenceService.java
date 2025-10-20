package com.commodities.matching.service;

import com.commodities.matching.entity.TradeEntity;
import com.commodities.matching.model.Trade;
import com.commodities.matching.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TradePersistenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(TradePersistenceService.class);
    private final TradeRepository tradeRepository;
    
    public TradePersistenceService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }
    
    @Async
    @Transactional
    public void persistTrade(Trade trade) {
        try {
            TradeEntity entity = new TradeEntity(
                trade.getTradeId(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getCommodity().getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getProcessingTimeNanos()
            );
            
            tradeRepository.save(entity);
            logger.debug("Trade {} persisted to database", trade.getTradeId());
        } catch (Exception e) {
            logger.error("Failed to persist trade {}", trade.getTradeId(), e);
        }
    }
    
    public List<TradeEntity> getRecentTrades(String commodity, int minutes) {
        Instant since = Instant.now().minusSeconds(minutes * 60L);
        return tradeRepository.findRecentTradesByCommodity(commodity, since);
    }
    
    public List<TradeEntity> getTradesByCommodity(String commodity) {
        return tradeRepository.findByCommodity(commodity);
    }
    
    public Long getTradeCountSince(Instant since) {
        return tradeRepository.countTradesSince(since);
    }
}
