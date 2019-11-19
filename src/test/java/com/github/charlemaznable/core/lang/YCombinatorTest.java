package com.github.charlemaznable.core.lang;

import com.github.charlemaznable.core.lang.YCombinator.CacheableUnaryOperator;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class YCombinatorTest {

    @Test
    public void testYCombinator() {
        List<Integer> facOrder = newArrayList();
        val fac = YCombinator.<Integer, Integer>of(
                recFunc -> n -> {
                    facOrder.add(n);
                    return n < 2 ? 1 : (n * recFunc.apply(n - 1));
                });
        facOrder.clear();
        assertEquals(3628800, fac.apply(10));
        assertEquals(10, facOrder.size());
        facOrder.clear();
        assertEquals(3628800, fac.apply(10));
        assertEquals(10, facOrder.size());

        List<Integer> fibOrder = newArrayList();
        val fib = YCombinator.<Integer, Integer>of(
                recFunc -> n -> {
                    fibOrder.add(n);
                    return n <= 2 ? 1 : (recFunc.apply(n - 1) + recFunc.apply(n - 2));
                });
        fibOrder.clear();
        assertEquals(55, fib.apply(10));
        assertEquals(109, fibOrder.size());
        fibOrder.clear();
        assertEquals(55, fib.apply(10));
        assertEquals(109, fibOrder.size());
    }

    @Test
    public void testYCombinatorCache() {
        List<Integer> facOrder = newArrayList();
        val fac = YCombinator.of(new CacheableUnaryOperator<Integer, Integer>() {
            @Override
            public Function<Integer, Integer> apply(Function<Integer, Integer> recFunc) {
                return n -> {
                    facOrder.add(n);
                    return n < 2 ? 1 : (n * recFunc.apply(n - 1));
                };
            }
        });
        facOrder.clear();
        assertEquals(3628800, fac.apply(10));
        assertEquals(10, facOrder.size());
        facOrder.clear();
        assertEquals(3628800, fac.apply(10));
        assertEquals(0, facOrder.size());

        List<Integer> fibOrder = newArrayList();
        val fib = YCombinator.of(new CacheableUnaryOperator<Integer, Integer>() {
            @Override
            public Function<Integer, Integer> apply(Function<Integer, Integer> recFunc) {
                return n -> {
                    fibOrder.add(n);
                    return n <= 2 ? 1 : (recFunc.apply(n - 1) + recFunc.apply(n - 2));
                };
            }
        });
        fibOrder.clear();
        assertEquals(55, fib.apply(10));
        assertEquals(10, fibOrder.size());
        fibOrder.clear();
        assertEquals(55, fib.apply(10));
        assertEquals(0, fibOrder.size());
    }
}
