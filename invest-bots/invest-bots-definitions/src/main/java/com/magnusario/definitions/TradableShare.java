package com.magnusario.definitions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"figi", "ticker"})
public class TradableShare {
    private final String figi;
    private final String ticker;
    private final String currency;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private final ZonedDateTime lastCheckingTime;
}
