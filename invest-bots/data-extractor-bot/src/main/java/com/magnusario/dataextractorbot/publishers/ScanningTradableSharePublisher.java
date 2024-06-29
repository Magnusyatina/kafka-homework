package com.magnusario.dataextractorbot.publishers;

import com.magnusario.dataextractorbot.Bot;
import org.springframework.stereotype.Component;

@Component
public class ScanningTradableSharePublisher implements MarketInformationPublisher<Bot.TradableShareScanInfo> {
    @Override
    public void publish(Bot.TradableShareScanInfo value) {

    }
}
