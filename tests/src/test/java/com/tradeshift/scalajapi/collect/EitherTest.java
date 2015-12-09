package com.tradeshift.scalajapi.collect;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EitherTest {
    @Test
    public void either_java_api_compiles() {
        final Either<String,Integer> s = Left.of("s");
        final Either<String,Integer> one = Right.of(1);
        
        assertEquals("left: s", s.fold(l -> "left: " + l, r -> "right: " + r));
        assertEquals("right: 1", one.fold(l -> "left: " + l, r -> "right: " + r));
        assertEquals("s", s.left().get());
    }
}
