package com.github.charlemaznable.core.config.guice;

import com.github.charlemaznable.core.config.EnvModular;
import com.google.inject.Guice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EnvGuiceTest {

    @Test
    public void testEnvGuice() {
        var envModular = new EnvModular().scanPackageClasses(TestEnvGuiceConfig.class);
        var injector = Guice.createInjector(envModular.createModule());

        var testEnvConfig = injector.getInstance(TestEnvGuiceConfig.class);
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
