package com.github.charlemaznable.core.lang;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YCombinatorTest {

    @Test
    public void testYCombinator() {
        new YCombinator();

        val fac = YCombinator.<Integer, Integer>of(
                recFunc -> n -> n < 2 ? 1 : (n * recFunc.apply(n - 1)));
        assertEquals(3628800, fac.apply(10));

        val fib = YCombinator.<Integer, Integer>of(
                recFunc -> n -> n <= 2 ? 1 : (recFunc.apply(n - 1) + recFunc.apply(n - 2)));
        assertEquals(55, fib.apply(10));
    }
}
