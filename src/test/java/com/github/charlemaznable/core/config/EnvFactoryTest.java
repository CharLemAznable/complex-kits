package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.EnvConfig.ConfigKeyProvider;
import com.github.charlemaznable.core.config.EnvConfig.DefaultValueProvider;
import com.github.charlemaznable.core.config.ex.ConfigValueFormatException;
import com.github.charlemaznable.core.config.ex.EnvConfigException;
import lombok.val;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvFactoryTest {

    @Test
    public void testConstruct() {
        assertThrows(ReflectException.class,
                () -> onClass(EnvFactory.class).create().get());
        assertThrows(EnvConfigException.class,
                () -> EnvFactory.getEnv(ErrorEnvConfig.class));
    }

    @Test
    public void testEnvConfig() {
        val testEnvConfig = EnvFactory.getEnv(TestEnvConfig.class);

        assertEquals("value1", testEnvConfig.key1());
        assertEquals("value2", testEnvConfig.key2());
        assertEquals("value3", testEnvConfig.key3());
        assertEquals("value4", testEnvConfig.key4());
        assertNull(testEnvConfig.key5());
        assertEquals("value5", testEnvConfig.key5Def());
        assertEquals("value5", testEnvConfig.key5("value5"));

        val springEnvConfig = EnvFactory.springEnvLoader().getEnv(TestEnvConfig.class);

        val custom1 = springEnvConfig.subset("custom1");
        assertEquals(springEnvConfig.custom1Key1(), custom1.getStr("key1"));
        assertEquals(springEnvConfig.custom1Key2(), custom1.getStr("key2"));

        val custom2 = springEnvConfig.subset("custom2");
        assertEquals(springEnvConfig.custom2Key1(), custom2.getStr("key1"));
        assertEquals(springEnvConfig.custom2Key2(), custom2.getStr("key2"));

        assertEquals(testEnvConfig, springEnvConfig);
        assertEquals(testEnvConfig.toString(), springEnvConfig.toString());

        val primEnvConfig = EnvFactory.getEnv(PrimEnvConfig.class);

        assertEquals(1, primEnvConfig.int1());
        assertEquals(1, primEnvConfig.short1());
        assertEquals(2L, primEnvConfig.long1());
        assertTrue(primEnvConfig.bool1());
        assertEquals(3F, primEnvConfig.float1());
        assertEquals(4D, primEnvConfig.double1());
        assertEquals(1, primEnvConfig.byte1());
        assertEquals('t', primEnvConfig.char1());

        assertEquals(0, primEnvConfig.int2());
        assertEquals(0, primEnvConfig.short2());
        assertEquals(0, primEnvConfig.long2());
        assertFalse(primEnvConfig.bool2());
        assertEquals(0, primEnvConfig.float2());
        assertEquals(0, primEnvConfig.double2());

        assertThrows(ConfigValueFormatException.class, primEnvConfig::int3);
        assertThrows(ConfigValueFormatException.class, primEnvConfig::short3);
        assertThrows(ConfigValueFormatException.class, primEnvConfig::long3);
        assertThrows(ConfigValueFormatException.class, primEnvConfig::float3);
        assertThrows(ConfigValueFormatException.class, primEnvConfig::double3);

        assertEquals(1, primEnvConfig.int2(1));
        assertEquals(1, primEnvConfig.short2((short) 1));
        assertEquals(2L, primEnvConfig.long2(2L));
        assertTrue(primEnvConfig.bool2(true));
        assertEquals(3F, primEnvConfig.float2(3F));
        assertEquals(4D, primEnvConfig.double2(4D));

        assertEquals(1, primEnvConfig.int3Def());
        assertEquals(1, primEnvConfig.short3Def());
        assertEquals(2L, primEnvConfig.long3Def());
        assertFalse(primEnvConfig.bool3Def());
        assertEquals(3F, primEnvConfig.float3Def());
        assertEquals(4D, primEnvConfig.double3Def());

        val beanEnvConfig = EnvFactory.getEnv(BeanEnvConfig.class);

        val bean1 = beanEnvConfig.bean1();
        assertEquals("value1", bean1.getKey1());
        assertEquals("value2", bean1.getKey2());

        val bean2 = beanEnvConfig.bean2();
        assertEquals("value1", bean2.getKey1());
        assertEquals("value2", bean2.getKey2());
        assertEquals("value1value2", bean2.getKey3());

        val bean3 = beanEnvConfig.bean3();
        assertEquals("value1", bean3.getKey1());
        assertEquals("value2", bean3.getKey2());
        assertEquals("value3", bean3.getKey3());

        val bean1List1 = beanEnvConfig.bean1List1();
        assertEquals(1, bean1List1.size());
        assertEquals("value1", bean1List1.get(0).getKey1());
        assertEquals("value2", bean1List1.get(0).getKey2());

        val bean1List2 = beanEnvConfig.bean1List2();
        assertEquals(2, bean1List2.size());
        assertEquals("value1", bean1List2.get(0).getKey1());
        assertEquals("value2", bean1List2.get(0).getKey2());
        assertEquals("value1value2", bean1List2.get(0).getKey3());
        assertEquals("value3", bean1List2.get(1).getKey1());
        assertEquals("value4", bean1List2.get(1).getKey2());
        assertEquals("value3value4", bean1List2.get(1).getKey3());

        val provEnvConfig = EnvFactory.getEnv(ProvEnvConfig.class);
        assertEquals("PROV", provEnvConfig.prov());
        assertThrows(EnvConfigException.class, provEnvConfig::error1);
        assertThrows(EnvConfigException.class, provEnvConfig::error2);

        val argEnvConfig = EnvFactory.getEnv(ArgEnvConfig.class);
        assertNull(argEnvConfig.custom1());
        assertNull(argEnvConfig.custom2());

        Arguments.initial("--customKey1=key1", "--customKey2=key2");
        assertEquals("value1", argEnvConfig.custom1());
        assertEquals("value1", argEnvConfig.custom2());

        Arguments.initial("--customKey1=key2", "--customKey2=key1");
        assertEquals("value2", argEnvConfig.custom1());
        assertEquals("value2", argEnvConfig.custom2());
    }

    @EnvConfig
    public interface TestEnvConfig extends Configable {

        String key1();

        String key2();

        String key3();

        @EnvConfig("testIni.key4")
        String key4();

        String key5();

        @EnvConfig(configKey = "key5", defaultValue = "value5")
        String key5Def();

        String key5(String defaultValue);

        @EnvConfig("custom1.key1")
        String custom1Key1();

        @EnvConfig("custom1.key2")
        String custom1Key2();

        @EnvConfig("custom2.key1")
        String custom2Key1();

        @EnvConfig("custom2.key2")
        String custom2Key2();
    }

    @EnvConfig
    public interface PrimEnvConfig {

        int int1();

        short short1();

        long long1();

        boolean bool1();

        float float1();

        double double1();

        @EnvConfig("int1")
        byte byte1();

        @EnvConfig("bool1")
        char char1();

        int int2();

        short short2();

        long long2();

        boolean bool2();

        float float2();

        double double2();

        int int3();

        short short3();

        long long3();

        boolean bool3();

        float float3();

        double double3();

        int int2(int def);

        short short2(short def);

        long long2(long def);

        boolean bool2(boolean def);

        float float2(float def);

        double double2(double def);

        @EnvConfig(configKey = "int3", defaultValue = "1")
        int int3Def();

        @EnvConfig(configKey = "short3", defaultValue = "1")
        short short3Def();

        @EnvConfig(configKey = "long3", defaultValue = "2L")
        long long3Def();

        @EnvConfig(configKey = "bool3", defaultValue = "true")
        boolean bool3Def();

        @EnvConfig(configKey = "float3", defaultValue = "3F")
        float float3Def();

        @EnvConfig(configKey = "double3", defaultValue = "4D")
        double double3Def();
    }

    @EnvConfig
    public interface BeanEnvConfig {

        ConfigBean bean1();

        ConfigBean2 bean2();

        ConfigBean2 bean3();

        List<ConfigBean> bean1List1();

        List<ConfigBean2> bean1List2();
    }

    @EnvConfig
    public interface ProvEnvConfig {

        @EnvConfig(configKeyProvider = Provider.class, defaultValueProvider = Provider.class)
        String prov();

        @EnvConfig(configKeyProvider = ErrorProvider.class)
        String error1();

        @EnvConfig(defaultValueProvider = ErrorProvider.class)
        String error2();
    }

    public static class Provider implements ConfigKeyProvider, DefaultValueProvider {

        @Override
        public String configKey(Class<?> minerClass, Method method) {
            return "prov";
        }

        @Override
        public String defaultValue(Class<?> minerClass, Method method) {
            return "PROV";
        }
    }

    @EnvConfig
    public interface ArgEnvConfig {

        @EnvConfig("custom1.${customKey1}")
        String custom1();

        @EnvConfig("custom2.${customKey2}")
        String custom2();
    }

    public static class ErrorProvider implements ConfigKeyProvider, DefaultValueProvider {}

    @EnvConfig
    public static class ErrorEnvConfig {}
}
