package com.magnusario.definitions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"figi", "ticker"})
public class TradableShare {
    private final String figi;
    private final String ticker;
    private final String currency;
}
