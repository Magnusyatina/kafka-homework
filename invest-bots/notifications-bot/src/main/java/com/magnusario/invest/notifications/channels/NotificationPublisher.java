package com.magnusario.invest.notifications.channels;

import com.magnusario.definitions.notifications.TradableShareNotification;

public interface NotificationPublisher {

    void publish(TradableShareNotification tradableShareNotification);
}
