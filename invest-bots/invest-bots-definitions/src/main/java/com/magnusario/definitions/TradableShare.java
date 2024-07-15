package com.magnusario.definitions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(of = {"figi", "ticker"})
public class TradableShare {
    private String figi;
    private String ticker;
    private String currency;
    private ZonedDateTime lastCheckingTime;

    @JsonCreator
    public TradableShare(
            @JsonProperty("figi") String figi,
            @JsonProperty("ticker") String ticker,
            @JsonProperty("currency") String currency,
            @JsonProperty("lastCheckingTime") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime lastCheckingTime) {
        this.figi = figi;
        this.ticker = ticker;
        this.currency = currency;
        this.lastCheckingTime = lastCheckingTime;
    }
}
