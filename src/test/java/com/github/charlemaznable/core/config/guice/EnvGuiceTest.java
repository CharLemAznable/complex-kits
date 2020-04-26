package com.github.charlemaznable.core.config.guice;

import com.github.charlemaznable.core.config.EnvModular;
import com.google.inject.Guice;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EnvGuiceTest {

    @Test
    public void testEnvGuice() {
        val envModular = new EnvModular().scanPackageClasses(TestEnvGuiceConfig.class);
        val injector = Guice.createInjector(envModular.createModule());

        val testEnvConfig = injector.getInstance(TestEnvGuiceConfig.class);
        assertEquals(envModular.getEnv(TestEnvGuiceConfig.class), testEnvConfig);

        assertEquals("value1", testEnvConfig.key1());
        assertEquals("value2", testEnvConfig.key2());
        assertEquals("value3", testEnvConfig.key3());
        assertEquals("value4", testEnvConfig.key4());
        assertNull(testEnvConfig.key5());
        assertEquals("value5", testEnvConfig.key5Def());
        assertEquals("value5", testEnvConfig.key5("value5"));
    }
}
