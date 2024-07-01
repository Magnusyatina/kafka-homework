package com.magnusario.definitions.notifications;

import java.time.ZonedDateTime;

public interface TradableShareNotification {
    String getFigi();

    String getTicker();

    String getCurrency();

    ZonedDateTime getLastCheckingTime();
}
