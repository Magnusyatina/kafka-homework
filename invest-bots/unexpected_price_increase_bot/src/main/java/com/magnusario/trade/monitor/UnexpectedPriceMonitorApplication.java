package com.magnusario.trade.monitor;

import com.magnusario.trade.monitor.config.SignalsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SignalsConfiguration.class})
public class UnexpectedPriceMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnexpectedPriceMonitorApplication.class, args);
    }
}
