package com.magnusario.invest.notifications.channels;

import com.magnusario.definitions.notifications.TradableShareNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationPublisherImpl implements NotificationPublisher, ChannelRegistrar<TradableShareNotification> {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisherImpl.class);

    private Set<Channel<TradableShareNotification>> channels = ConcurrentHashMap.newKeySet();

    @Override
    public boolean containsChannel(String channelId) {
        return channels.stream().anyMatch(e -> Objects.equals(e.getChannelId(), channelId));
    }

    @Override
    public void register(Channel<TradableShareNotification> channel) {
        logger.info("Registrate new channel for InvestNotifications with id {}", channel.getChannelId());
        channels.add(channel);
    }

    @Override
    public void publish(TradableShareNotification notification) {
        for (Channel<TradableShareNotification> channel : channels) {
            logger.info("Pushing new notification for figi {} into channel {}", notification.getFigi(), channel.getChannelId());
            channel.push(notification);
        }
    }
}
