package com.github.charlemaznable.codec;

import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Json.trans;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JsonTest {

    @Test
    public void testTrans() {
        val beanType11 = new BeanType1();
        beanType11.setValue1("value1");
        beanType11.setValue2("value2");

        val beanType2 = trans(beanType11, BeanType2.class);
        assertEquals("value2", beanType2.getValue2());
        assertNull(beanType2.getValue3());

        val beanType12 = trans(beanType2, BeanType1.class);
        assertNull(beanType12.getValue1());
        assertEquals("value2", beanType12.getValue2());
    }

    @Data
    static class BeanType1 {
        private String value1;
        private String value2;
    }

    @Data
    static class BeanType2 {
        private String value2;
        private String value3;
    }
}
