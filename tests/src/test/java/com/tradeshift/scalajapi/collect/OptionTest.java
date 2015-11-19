package com.tradeshift.scalajapi.collect;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionTest {
    @Test
    public void option_should_return_typesafe_iterator() {
        Option<String> option = Option.of("hello");
        Iterator<String> i = option.iterator();
        assertTrue(i.hasNext());
    }

    @Test
    public void option_is_iterable() {
        Option<String> option = Option.of("hello");
        for (String s: option) {
            assertEquals("hello", s);
        }
    }
    
}
