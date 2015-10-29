package com.tradeshift.scalajapi.concurrent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ConcurrentAssertions {
    private final Duration defaultTimeout;

    public ConcurrentAssertions() {
        this(Duration.of(3, ChronoUnit.SECONDS));
    }
    
    public ConcurrentAssertions(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }
    
    public <T> FutureAssert<T> assertThat(Future<T> future) {
        return new FutureAssert<>(future, defaultTimeout);
    }
}
