package com.magnusario.trade.monitor.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Set;

@ConfigurationProperties(prefix = "signals")
@Getter
@Setter
@Validated
public class SignalsConfiguration {
    @Valid
    private Set<SignalMeta> meta;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class SignalMeta {
        @NotNull
        private Double percentLimitation;
        @NotNull
        private Integer weight;
        @NotNull
        private Duration cleanInterval;
    }
}
