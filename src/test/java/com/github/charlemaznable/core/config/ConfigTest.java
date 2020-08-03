package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.ex.ConfigNotFoundException;
import com.github.charlemaznable.core.config.ex.ConfigValueFormatException;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigTest {

    @Test
    public void testConfig() {
        assertEquals("value1", Config.getStr("key1"));
        assertEquals("value2", Config.getStr("key2"));
        assertEquals("value3", Config.getStr("key3"));
        assertEquals("value4", Config.getStr("testIni.key4"));
        assertNull(Config.getStr("key5"));
        assertEquals("value5", Config.getStr("key5", "value5"));

        val custom1 = Config.subset("custom1");
        assertEquals(Config.getStr("custom1.key1"), custom1.getStr("key1"));
        assertEquals(Config.getStr("custom1.key2"), custom1.getStr("key2"));

        val custom2 = Config.subset("custom2");
        assertEquals(Config.getStr("custom2.key1"), custom2.getStr("key1"));
        assertEquals(Config.getStr("custom2.key2"), custom2.getStr("key2"));

        assertEquals(1, Config.getInt("int1"));
        assertEquals(2L, Config.getLong("long1"));
        assertTrue(Config.getBool("bool1"));
        assertEquals(3F, Config.getFloat("float1"));
        assertEquals(4D, Config.getDouble("double1"));

        assertThrows(ConfigNotFoundException.class, () -> Config.getInt("int2"));
        assertThrows(ConfigNotFoundException.class, () -> Config.getLong("long2"));
        assertThrows(ConfigNotFoundException.class, () -> Config.getBool("bool2"));
        assertThrows(ConfigNotFoundException.class, () -> Config.getFloat("float2"));
        assertThrows(ConfigNotFoundException.class, () -> Config.getDouble("double2"));

        assertThrows(ConfigValueFormatException.class, () -> Config.getInt("int3"));
        assertThrows(ConfigValueFormatException.class, () -> Config.getLong("long3"));
        assertThrows(ConfigValueFormatException.class, () -> Config.getFloat("float3"));
        assertThrows(ConfigValueFormatException.class, () -> Config.getDouble("double3"));

        assertEquals(1, Config.getInt("int2", 1));
        assertEquals(2L, Config.getLong("long2", 2L));
        assertTrue(Config.getBool("bool2", true));
        assertEquals(3F, Config.getFloat("float2", 3F));
        assertEquals(4D, Config.getDouble("double2", 4D));

        assertEquals(1, Config.getInt("int3", 1));
        assertEquals(2L, Config.getLong("long3", 2L));
        assertFalse(Config.getBool("bool3", true));
        assertEquals(3F, Config.getFloat("float3", 3F));
        assertEquals(4D, Config.getDouble("double3", 4D));

        val bean1 = Config.getBean("bean1", ConfigBean.class);
        assertEquals("value1", bean1.getKey1());
        assertEquals("value2", bean1.getKey2());

        val bean2 = Config.getBean("bean2", ConfigBean2.class);
        assertEquals("value1", bean2.getKey1());
        assertEquals("value2", bean2.getKey2());
        assertEquals("value1value2", bean2.getKey3());

        val bean3 = Config.getBean("bean3", ConfigBean2.class);
        assertEquals("value1", bean3.getKey1());
        assertEquals("value2", bean3.getKey2());
        assertEquals("value3", bean3.getKey3());

        val bean1List1 = Config.getBeans("bean1List1", ConfigBean.class);
        assertEquals(1, bean1List1.size());
        assertEquals("value1", bean1List1.get(0).getKey1());
        assertEquals("value2", bean1List1.get(0).getKey2());

        val bean1List2 = Config.getBeans("bean1List2", ConfigBean2.class);
        assertEquals(2, bean1List2.size());
        assertEquals("value1", bean1List2.get(0).getKey1());
        assertEquals("value2", bean1List2.get(0).getKey2());
        assertEquals("value1value2", bean1List2.get(0).getKey3());
        assertEquals("value3", bean1List2.get(1).getKey1());
        assertEquals("value4", bean1List2.get(1).getKey2());
        assertEquals("value3value4", bean1List2.get(1).getKey3());
    }
}
