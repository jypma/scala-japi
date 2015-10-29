package com.tradeshift.scalajapi.concurrent;

public class Assertions {
    private static ConcurrentAssertions concurrent = new ConcurrentAssertions();

    public static <T> FutureAssert<T> assertThat(Future<T> future) {
        return concurrent.assertThat(future);
    }
}
