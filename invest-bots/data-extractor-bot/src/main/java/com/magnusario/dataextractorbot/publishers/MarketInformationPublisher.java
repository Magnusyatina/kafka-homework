package com.magnusario.dataextractorbot.publishers;

public interface MarketInformationPublisher<T> {

    void publish(T value);
}
