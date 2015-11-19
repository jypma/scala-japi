package com.tradeshift.scalajapi.collect;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

public class SeqTest {
    @Test
    public void seq_should_return_typesafe_iterator() {
        Seq<String> seq = Seq.of("hello");
        Iterator<String> i = seq.iterator();
        assertTrue(i.hasNext());
    }

    @Test
    public void seq_is_iterable() {
        Seq<String> seq = Seq.of("hello");
        for (String s: seq) {
            assertEquals("hello", s);
        }
    }
    
}
