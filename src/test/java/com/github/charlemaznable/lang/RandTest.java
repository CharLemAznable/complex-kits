package com.github.charlemaznable.lang;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.github.charlemaznable.lang.Rand.randInverseWeighted;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandTest {

    @Test
    public void testRandWeighted() {
        new Rand();
        Map<String, Integer> weightMap = newHashMap();
        weightMap.put("1", 0);
        weightMap.put("2", 0);
        weightMap.put("3", 0);
        weightMap.put("4", 0);
        int count = 100000;
        double percent = 1. / weightMap.size();

        for (int i = 0; i < count; i++) {
            String item = randInverseWeighted(weightMap);
            weightMap.put(item, weightMap.get(item) + 1);
        }

        assertEquals(percent, new BigDecimal(1. * weightMap.get("1") / count).doubleValue(), 0.01);
        assertEquals(percent, new BigDecimal(1. * weightMap.get("2") / count).doubleValue(), 0.01);
        assertEquals(percent, new BigDecimal(1. * weightMap.get("3") / count).doubleValue(), 0.01);
        assertEquals(percent, new BigDecimal(1. * weightMap.get("4") / count).doubleValue(), 0.01);
    }
}
