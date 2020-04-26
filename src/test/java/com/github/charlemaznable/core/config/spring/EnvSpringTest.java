package com.github.charlemaznable.core.config.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EnvSpringConfiguration.class)
public class EnvSpringTest {

    @Autowired
    private TestEnvSpringConfig testEnvConfig;

    @Test
    public void testEnvSpring() {
        assertEquals("value1", testEnvConfig.key1());
        assertEquals("value2", testEnvConfig.key2());
        assertEquals("value3", testEnvConfig.key3());
        assertEquals("value4", testEnvConfig.key4());
        assertNull(testEnvConfig.key5());
        assertEquals("value5", testEnvConfig.key5Def());
        assertEquals("value5", testEnvConfig.key5("value5"));
    }
}
