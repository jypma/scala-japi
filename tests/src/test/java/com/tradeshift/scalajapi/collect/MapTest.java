package com.tradeshift.scalajapi.collect;

import java.util.Iterator;

import org.junit.Test;

import scala.Tuple2;
import static org.junit.Assert.*;

public class MapTest {
    @Test
    public void map_should_return_typesafe_iterator() {
        Map<String,String> map = Map.of("hello", "world");
        Iterator<Tuple2<String,String>> i = map.iterator();
        assertTrue(i.hasNext());
    }

    @Test
    public void seq_is_iterable() {
        Map<String,String> map = Map.of("hello", "world");
        for (Tuple2<String,String> s: map) {
            assertEquals(Tuple2.apply("hello", "world"), s);
        }
    }
    
}
