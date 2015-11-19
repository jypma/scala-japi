package com.tradeshift.scalajapi.collect;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

public class SetTest {
    @Test
    public void set_should_return_typesafe_iterator() {
        Set<String> set = Set.of("hello");
        Iterator<String> i = set.iterator();
        assertTrue(i.hasNext());
    }

    @Test
    public void set_is_iterable() {
        Set<String> set = Set.of("hello");
        for (String s: set) {
            assertEquals("hello", s);
        }
    }
    
}
