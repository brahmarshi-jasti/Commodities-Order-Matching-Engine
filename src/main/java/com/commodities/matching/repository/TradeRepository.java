package com.commodities.matching.repository;

import com.commodities.matching.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    
    List<TradeEntity> findByCommodity(String commodity);
    
    List<TradeEntity> findByTimestampBetween(Instant start, Instant end);
    
    @Query("SELECT t FROM TradeEntity t WHERE t.commodity = ?1 AND t.timestamp >= ?2 ORDER BY t.timestamp DESC")
    List<TradeEntity> findRecentTradesByCommodity(String commodity, Instant since);
    
    @Query("SELECT COUNT(t) FROM TradeEntity t WHERE t.timestamp >= ?1")
    Long countTradesSince(Instant since);
}
