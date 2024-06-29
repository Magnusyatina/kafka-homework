package com.magnusario.definitions.aggregators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magnusario.definitions.LastPrice;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PriceAggregator {
    private String figi;
    private Duration cleanInterval = Duration.ZERO;

    private LinkedList<LastPrice> lastPrices = new LinkedList<>();
    private LastPrice currentPrice;
    @JsonIgnore
    private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public PriceAggregator() {
    }

    public PriceAggregator(Duration cleanInterval) {
        this.cleanInterval = cleanInterval;
    }

    public PriceAggregator addLastPrice(String figi, LastPrice lastPrice) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            if (this.figi == null)
                this.figi = figi;
            if (currentPrice != null)
                lastPrices.add(currentPrice);
            currentPrice = lastPrice;
            if (isNeedCleaning(cleanInterval))
                clean(cleanInterval);
            return this;
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public boolean isNeedCleaning(Duration duration) {
        reentrantReadWriteLock.readLock().lock();
        try {
            if (lastPrices.isEmpty())
                return false;
            LastPrice first = lastPrices.getFirst();
            ZonedDateTime timeLimitation = ZonedDateTime.now().minus(duration);
            return first.getZonedDateTime().isBefore(timeLimitation);
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    public void clean(Duration duration) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            ZonedDateTime timeLimitation = ZonedDateTime.now().minus(duration);
            lastPrices = lastPrices
                    .stream()
                    .filter(e -> e.getZonedDateTime().isBefore(timeLimitation))
                    .collect(Collectors.toCollection(LinkedList::new));
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public BigDecimal getAverage() {
        LinkedList<LastPrice> currentPrices = new LinkedList<>(lastPrices);
        if (currentPrices.isEmpty())
            return currentPrice == null ? BigDecimal.ZERO : currentPrice.getPrice();
        BigDecimal sum = currentPrices.stream().map(LastPrice::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(currentPrices.size()), RoundingMode.HALF_EVEN);
    }
}
