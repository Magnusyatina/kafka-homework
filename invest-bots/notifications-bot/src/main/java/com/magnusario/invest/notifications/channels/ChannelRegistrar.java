package com.magnusario.invest.notifications.channels;

public interface ChannelRegistrar<M> {

    void register(Channel<M> channel);

    boolean containsChannel(String channelId);
}
