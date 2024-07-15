package com.magnusario.definitions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Signal {
    private String figi;
    private String message;
    private Integer weight;
    private String signalSource;
}
