package com.tradeshift.scalajapi.collect;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

public class SortedSetTest {
    @Test
    public void sorted_set_should_return_typesafe_iterator() {
        SortedSet<String> set = SortedSet.of("hello");
        Iterator<String> i = set.iterator();
        assertTrue(i.hasNext());
    }

    @Test
    public void sorted_set_is_iterable() {
        SortedSet<String> set = SortedSet.of("hello");
        for (String s: set) {
            assertEquals("hello", s);
        }
    }
    
}
