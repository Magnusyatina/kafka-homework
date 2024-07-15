package com.magnusario.definitions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastPrice {
    private String figi;
    private BigDecimal price;
    private String currency;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime zonedDateTime;
}
