package com.magnusario.invest.notifications.channels;

public interface Channel<M> {

    String getChannelId();

    void push(M m);
}
