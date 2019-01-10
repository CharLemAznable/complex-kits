package com.github.charlemaznable.spring;

import com.github.charlemaznable.spring.testClass.TestClass;
import com.github.charlemaznable.spring.testClass.TestConfiguration;
import com.github.charlemaznable.spring.testClass.TestSpringContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class SpringContextTest {

    @Test
    public void testSpringContext() {
        TestClass testClassBean = TestSpringContext.getBean(TestClass.class);
        assertNotNull(testClassBean);

        ClzResolver clzResolverBean = TestSpringContext.getBean(ClzResolver.class);
        assertNull(clzResolverBean);
    }
}
