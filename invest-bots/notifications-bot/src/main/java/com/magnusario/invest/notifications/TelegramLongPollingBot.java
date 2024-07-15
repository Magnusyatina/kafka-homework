package com.magnusario.invest.notifications;

import com.magnusario.definitions.notifications.TradableShareNotification;
import com.magnusario.invest.notifications.channels.ChannelRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class TelegramLongPollingBot implements SpringLongPollingBot {

    @Autowired
    private ChannelRegistrar<TradableShareNotification> channelRegistrar;

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return new InvestNotificationsBot(
                new OkHttpTelegramClient(getBotToken()),
                "MagniInvestBot",
                channelRegistrar);
    }
}
