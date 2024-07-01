package com.magnusario.definitions.notifications;

import com.magnusario.definitions.Signal;

import java.util.Set;

public interface SignalsNotification {
    String getFigi();

    Set<Signal> getSignals();

    InvestmentForecast getInvestmentForecast();
}
