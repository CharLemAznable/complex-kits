package com.github.charlemaznable.codec;

import com.github.charlemaznable.lang.Mapp;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.charlemaznable.codec.Json.jsonOf;
import static com.github.charlemaznable.codec.Json.jsonWithType;
import static com.github.charlemaznable.codec.Json.spec;
import static com.github.charlemaznable.codec.Json.trans;
import static com.github.charlemaznable.codec.Json.unJson;
import static com.github.charlemaznable.codec.Json.unJsonArray;
import static com.github.charlemaznable.codec.Json.unJsonWithType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonTest {

    @Test
    public void testJson() {
        new Json();

        val beanType11 = new BeanType1();
        beanType11.setValue1("value1");
        beanType11.setValue2("value2");
        String jsonWithType = jsonWithType(beanType11);
        assertTrue(jsonWithType.contains("\"@type\":\"com.github.charlemaznable.codec.JsonTest$BeanType1\""));

        val unJsonWithType = unJsonWithType(jsonWithType);
        assertTrue(unJsonWithType instanceof BeanType1);

        val jsonOf = jsonOf("key", "value");
        assertEquals("{\"key\":\"value\"}", jsonOf);
        Map<String, Object> unJsonMap = unJson(jsonOf);
        assertEquals(Mapp.of("key", "value"), unJsonMap);

        val listJson = "[{\"value1\":\"value1\",\"value2\":\"value2\"}]";
        val list1 = unJsonArray(listJson);
        assertEquals(1, list1.size());
        assertFalse(list1.get(0) instanceof BeanType1);
        val list2 = unJsonArray(listJson, BeanType1.class);
        assertEquals(1, list2.size());
        assertNotNull(list2.get(0));

        val specMap = Mapp.of("value1", "value1", "value2", "value2");
        BeanType1 specBean = spec(specMap, BeanType1.class);
        assertEquals("value1", specBean.getValue1());
        assertEquals("value2", specBean.getValue2());
    }

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
