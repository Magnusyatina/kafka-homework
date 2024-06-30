package com.magnusario.dataextractorbot;

import com.google.protobuf.Timestamp;
import com.magnusario.dataextractorbot.publishers.MarketInformationPublisher;
import com.magnusario.definitions.TradableShare;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.stream.StreamProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Autowired
    private InvestApi investApi;

    @Autowired
    private MarketInformationPublisher<com.magnusario.definitions.LastPrice> lastPricePublisher;

    @Autowired
    private MarketInformationPublisher<TradableShare> tradableSharePublisher;

    private Map<String, TradableShareScanInfo> tradableShares;

    @PostConstruct
    public void init() {
        tradableShares = investApi.getInstrumentsService()
                .getTradableSharesSync()
                .stream()
                .map(e -> new TradableShareScanInfo(
                        new TradableShare(e.getFigi(), e.getTicker(), e.getCurrency(), ZonedDateTime.now(ZoneId.systemDefault())),
                        false))
                .collect(Collectors.toConcurrentMap(TradableShareScanInfo::getFigi, e -> e, (e1, e2) -> e1));
    }

    @Scheduled(fixedDelay = 10000)
    public void schedule() {
        List<String> tradableInstuments = tradableShares
                .values()
                .stream()
                .filter(e -> !e.isScanning()).peek(e -> e.scanning = true)
                .map(TradableShareScanInfo::getFigi).collect(Collectors.toList());
        if (tradableInstuments.isEmpty())
            return;
        investApi.getMarketDataStreamService().newStream("last_prices_stream", getMarketDataResponseStreamProcessor(), (e) -> {
            logger.warn(e.getLocalizedMessage());
        }).subscribeLastPrices(tradableInstuments);
    }

    private StreamProcessor<MarketDataResponse> getMarketDataResponseStreamProcessor() {
        return (MarketDataResponse response) -> {
            if (response.hasSubscribeLastPriceResponse()) {
                response.getSubscribeLastPriceResponse().getLastPriceSubscriptionsList()
                        .forEach(e -> {
                            SubscriptionStatus subscriptionStatus = e.getSubscriptionStatus();
                            TradableShareScanInfo tradableShareScanInfo = tradableShares.get(e.getFigi());
                            publishTradableShare(tradableShareScanInfo);
                            if (SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS != subscriptionStatus) {
                                logger.info("Unsuccessful susbcribe for instrument %s with status %s".formatted(e.getFigi(), e.getSubscriptionStatus()));
                                //tradableShareScanInfo.setScanning(false);
                                return;
                            }
                            logger.info("Subscribe for instrument %s".formatted(e.getFigi()));
                        });
            } else if (response.hasLastPrice()) {
                LastPrice lastPrice = response.getLastPrice();
                Timestamp lastPriceTimestamp = lastPrice.getTime();
                TradableShareScanInfo tradableShareScanInfo = tradableShares.get(lastPrice.getFigi());
                BigDecimal lastPriceValue = parseValue(lastPrice.getPrice().getUnits(), lastPrice.getPrice().getNano());
                logger.info(STR."Publishing last price for \{lastPrice.getFigi()}. Last price is \{lastPriceValue}");
                publishTradableShare(tradableShareScanInfo);
                lastPricePublisher.publish(new com.magnusario.definitions.LastPrice(
                        tradableShareScanInfo.getFigi(),
                        lastPriceValue,
                        tradableShareScanInfo.getCurrency(),
                        ZonedDateTime.ofInstant(
                                Instant.ofEpochSecond(
                                        lastPriceTimestamp.getSeconds(),
                                        lastPriceTimestamp.getNanos()),
                                ZoneId.systemDefault())));

            }
        };
    }

    private void publishTradableShare(TradableShareScanInfo tradableShareScanInfo) {
        tradableSharePublisher.publish(new TradableShare(
                tradableShareScanInfo.getFigi(),
                tradableShareScanInfo.getTicker(),
                tradableShareScanInfo.getCurrency(),
                ZonedDateTime.now(ZoneId.systemDefault())));
    }

    private BigDecimal parseValue(long units, int nano) {
        return new BigDecimal(STR."\{units}.\{nano}");
    }

    @PreDestroy
    public void destroy() {
        List<String> tradableInstruments = tradableShares.values().stream().map(TradableShareScanInfo::getFigi).collect(Collectors.toList());
        logger.info("Unsubscribe from tradableInstruments");
        investApi.getMarketDataStreamService().getAllStreams().values().forEach(e -> e.unsubscribeLastPrices(tradableInstruments));
    }

    @Data
    @AllArgsConstructor
    public static class TradableShareScanInfo {
        @EqualsAndHashCode.Include
        private final TradableShare tradableShare;
        @EqualsAndHashCode.Exclude
        private boolean scanning;

        @EqualsAndHashCode.Include
        public String getFigi() {
            return tradableShare.getFigi();
        }

        @EqualsAndHashCode.Include
        public String getTicker() {
            return tradableShare.getTicker();
        }

        public String getCurrency() {
            return tradableShare.getCurrency();
        }
    }

}
