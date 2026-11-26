package com.folio.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All "now" and "today" in the app comes from this clock (zone {@code Asia/Kolkata} by default), never from
 * {@code LocalDate.now()}. Tests replace it with a fixed clock so due dates and overdue maths are deterministic.
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    Clock clock(FolioProperties properties) {
        return Clock.system(ZoneId.of(properties.timeZone()));
    }
}
