package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.codec.Xml.xml;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlTest {

    @Test
    public void testXml() {
        Map<String, Object> map = newHashMap();
        String xml = xml(map);
        assertEquals(map, unXml(xml));

        map.put("name1", "value1");
        xml = xml(map);
        assertEquals(map, unXml(xml));

        map.put("name2", "<value2>");
        xml = xml(map);
        assertEquals(map, unXml(xml));

        map.put("null", null);
        xml = xml(map);
        map.remove("null");
        assertEquals(map, unXml(xml));

        map.put("name3", "function matchwo(a,b){if(a<b&&a<0)then{return 1}else{return 0}}");
        xml = xml(map);
        assertEquals(map, unXml(xml));

        map.put("name4", newArrayList("value41", "value42"));
        xml = xml(map);
        assertEquals(map, unXml(xml));

        map.put("name5", of("key5", "value5"));
        xml = xml(map);
        assertEquals(map, unXml(xml));

        String rootXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
        String k12xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<xml>\n  <k1><![CDATA[k2]]></k1>\n</xml>\n";

        assertEquals(rootXml, xml(newHashMap(), "root"));
        assertEquals(k12xml, xml(of("k1", "k2"), true));

        assertEquals(newHashMap(), unXml(rootXml));
        assertEquals(of("xml", of("k1", "k2")), unXml(k12xml, true));
    }

    @Test
    public void testUnXml() {
        Map<String, Object> map = newHashMap();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml/>";
        assertEquals(map, unXml(xml));
        xml = "<xml/>";
        assertEquals(map, unXml(xml));

        map.put("name1", "value1");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<xml><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));

        map.put("name2", "<value2>");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><name2><![CDATA[<value2>]]></name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<xml><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<xml><name2><![CDATA[<value2>]]></name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));

        map.put("name3", "function matchwo(a,b){if(a<b&&a<0)then{return 1}else{return 0}}");
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><name3>function matchwo(a,b){if(a&lt;b&amp;&amp;a&lt;0)then{return 1}else{return 0}}</name3><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><name3><![CDATA[function matchwo(a,b){if(a<b&&a<0)then{return 1}else{return 0}}]]></name3><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<xml><name3>function matchwo(a,b){if(a&lt;b&amp;&amp;a&lt;0)then{return 1}else{return 0}}</name3><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
        xml = "<xml><name3><![CDATA[function matchwo(a,b){if(a<b&&a<0)then{return 1}else{return 0}}]]></name3><name2>&lt;value2&gt;</name2><name1>value1</name1></xml>";
        assertEquals(map, unXml(xml));
    }
}
