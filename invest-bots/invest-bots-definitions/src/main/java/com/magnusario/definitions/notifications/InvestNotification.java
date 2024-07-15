package com.magnusario.definitions.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magnusario.definitions.Signal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class InvestNotification {
    private String figi;
    private InvestmentForecast investmentForecast;
    private List<Signal> signals;
}
