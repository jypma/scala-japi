package com.tradeshift.scalajapi.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class ConcurrentTest {
    @Test
    public void blocking_can_be_invoked_with_lambda_returning_value() {
        int result = Concurrent.blocking(() -> 5);
        assertEquals(5, result);
    }
    
    @Test
    public void blocking_can_be_invoked_with_Runnable_lambda() {
        AtomicBoolean invoked = new AtomicBoolean(false);
        Concurrent.blocking(() -> {
            invoked.set(true);
        });
        assertTrue(invoked.get());
    }
}
