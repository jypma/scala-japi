package com.tradeshift.scalajapi.concurrent;

import java.time.Duration;

public class FutureAssert<T> extends AbstractFutureAssert<FutureAssert<T>, T> {
    protected FutureAssert(Future<T> actual, Duration timeout) {
        super(actual, FutureAssert.class, timeout);
    }
}
