package com.magnusario.invest.notifications.channels;

import com.magnusario.definitions.notifications.SignalsNotification;
import com.magnusario.definitions.notifications.TradableShareNotification;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@EqualsAndHashCode(of = {"channelId"})
public class InvestNotificationChannel implements Channel<TradableShareNotification> {

    private final String channelId;

    private final TelegramClient telegramClient;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public InvestNotificationChannel(String channelId, TelegramClient telegramClient) {
        this.channelId = channelId;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    @SneakyThrows
    public void push(TradableShareNotification tradableShareNotification) {
        telegramClient.execute(formSendMessage(tradableShareNotification));
    }

    @NotNull
    private SendMessage formSendMessage(TradableShareNotification tradableShareNotification) {
        String figi = tradableShareNotification.getFigi();
        return new SendMessage(channelId, formMessageText(tradableShareNotification));
    }

    @NotNull
    private String formMessageText(TradableShareNotification tradableShareNotification) {
        String message = STR."""
        Уведомление об активе, на которое следует обратить внимание:

        Идентификатор: \{tradableShareNotification.getFigi()}
        Тикер: \{tradableShareNotification.getTicker()}
        Расчетная валюта: \{tradableShareNotification.getCurrency()}
        Последнее время обновления данных: \{dateTimeFormatter.format(tradableShareNotification.getLastCheckingTime())}
        """;
        if (tradableShareNotification instanceof SignalsNotification signalsNotification) {
            String additionalInformation = signalsNotification
                    .getSignals()
                    .stream()
                    .map(e -> STR."Сигнал: \{e.getSignalSource()}, Описание: \{e.getMessage()}, Важность: \{e.getWeight()}")
                    .collect(Collectors.joining("\n"));
            message += STR."""
                    Прогноз: \{signalsNotification.getInvestmentForecast()}
                    Детали:
                    \{additionalInformation}
                    """;
        }
        return message;
    }
}
