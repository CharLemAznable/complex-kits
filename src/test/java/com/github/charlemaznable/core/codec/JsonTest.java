package com.github.charlemaznable.core.codec;

import com.alibaba.fastjson.parser.ParserConfig;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.descFlat;
import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.jsonDetectRef;
import static com.github.charlemaznable.core.codec.Json.jsonOf;
import static com.github.charlemaznable.core.codec.Json.jsonPretty;
import static com.github.charlemaznable.core.codec.Json.jsonWithType;
import static com.github.charlemaznable.core.codec.Json.spec;
import static com.github.charlemaznable.core.codec.Json.trans;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Json.unJsonArray;
import static com.github.charlemaznable.core.codec.Json.unJsonWithType;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonTest {

    @Test
    public void testJson() {
        val beanType11 = new BeanType1();
        beanType11.setValue1("value1");
        beanType11.setValue2("value2");
        String jsonWithType = jsonWithType(beanType11);
        assertTrue(jsonWithType.contains("\"@type\":\"com.github.charlemaznable.core.codec.JsonTest$BeanType1\""));

        ParserConfig.getGlobalInstance().addAccept("com.github.charlemaznable.core.codec.");
        val unJsonWithType = unJsonWithType(jsonWithType);
        assertTrue(unJsonWithType instanceof BeanType1);

        assertEquals("{\n" +
                "\t\"value1\":\"value1\",\n" +
                "\t\"value2\":\"value2\"\n" +
                "}", jsonPretty(beanType11));

        Map<String, Object> data = of("key", "value");
        Map<String, Object> wrap = of("data1", data, "data2", data);
        assertNotEquals(jsonDetectRef(wrap), json(wrap));

        val jsonOf = jsonOf("key", "value");
        assertEquals("{\"key\":\"value\"}", jsonOf);
        Map<String, Object> unJsonMap = unJson(jsonOf);
        assertEquals(of("key", "value"), unJsonMap);

        val listJson = "[{\"value1\":\"value1\",\"value2\":\"value2\"}]";
        val list1 = unJsonArray(listJson);
        assertEquals(1, list1.size());
        assertFalse(list1.get(0) instanceof BeanType1);
        val list2 = unJsonArray(listJson, BeanType1.class);
        assertEquals(1, list2.size());
        assertNotNull(list2.get(0));

        val specMap = of("value1", "value1", "value2", "value2");
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

    @Test
    public void testDescFlat() {
        val mapStr = of("cc", "dd");
        assertEquals(of("cc", "dd"), descFlat(mapStr));

        val bean11 = new BeanType1();
        bean11.setValue1("v11");
        bean11.setValue2("v12");
        val bean12 = new BeanType1();
        bean12.setValue1("v21");
        bean12.setValue2("v22");

        val mapBean = of("ee", bean11, "ff", bean12);
        assertEquals(of("ee.value1", "v11", "ee.value2", "v12",
                "ff.value1", "v21", "ff.value2", "v22"), descFlat(mapBean));

        val bean2 = new BeanType2();
        bean2.setValue2("v2");
        bean2.setValue3("v3");
        assertEquals(of("value2", "v2", "value3", "v3"), descFlat(bean2));

        val complex = new ComplexType();
        complex.setName("name");
        complex.setListStr(newArrayList("aa", "bb"));
        complex.setListBean(newArrayList(bean11, bean12));
        complex.setMapStr(mapStr);
        complex.setMapBean(mapBean);
        complex.setBean2(bean2);
        val expected = of("name", "name",
                "listStr[0]", "aa", "listStr[1]", "bb",
                "listBean[0].value1", "v11",
                "listBean[0].value2", "v12",
                "listBean[1].value1", "v21",
                "listBean[1].value2", "v22",
                "mapStr.cc", "dd",
                "mapBean.ee.value1", "v11",
                "mapBean.ee.value2", "v12",
                "mapBean.ff.value1", "v21",
                "mapBean.ff.value2", "v22",
                "bean2.value2", "v2", "bean2.value3", "v3");
        assertEquals(expected, descFlat(complex));
        assertTrue(descFlat(new EmptyType()).isEmpty());
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

    @Data
    static class ComplexType {

        private String name;
        private List<String> listStr;
        private List<BeanType1> listBean;
        private Map<String, String> mapStr;
        private Map<String, BeanType1> mapBean;
        private BeanType2 bean2;
    }

    @Data
    static class EmptyType {}
}
