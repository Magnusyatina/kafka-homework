package com.magnusario.invest.notifications;

import com.magnusario.definitions.notifications.TradableShareNotification;
import com.magnusario.invest.notifications.channels.ChannelRegistrar;
import com.magnusario.invest.notifications.channels.InvestNotificationChannel;
import lombok.SneakyThrows;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class InvestNotificationsBot extends AbilityBot {

    private final ChannelRegistrar<TradableShareNotification> channelRegistrar;

    public InvestNotificationsBot(TelegramClient telegramClient, String botUsername, ChannelRegistrar<TradableShareNotification> channelRegistrar) {
        super(telegramClient, botUsername);
        this.channelRegistrar = channelRegistrar;
    }

    @Override
    public long creatorId() {
        return 0;
    }

    @Override
    @SneakyThrows
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                String text = update.getMessage().getText();
                if (text.startsWith("/start")) {
                    telegramClient.execute(new SendMessage(chatId.toString(), "Hello, I'm super invest notification bot!!!"));
                } else if (text.startsWith("/subscribe")) {
                    channelRegistrar.register(new InvestNotificationChannel(chatId.toString(), telegramClient));
                }
            }
        }
    }
}
