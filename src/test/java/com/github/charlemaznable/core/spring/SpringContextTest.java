package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testClass.TestClass;
import com.github.charlemaznable.core.spring.testClass.TestConfiguration;
import com.github.charlemaznable.core.spring.testClass.TestMultiClass;
import com.github.charlemaznable.core.spring.testClass.TestSpringContext;
import lombok.var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class SpringContextTest {

    @Test
    public void testSpringContext() {
        var testClassBean = TestSpringContext.getBean(TestClass.class);
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean("TestClass");
        assertNotNull(testClassBean);

        var clzResolverBean = TestSpringContext.getBean(ClzResolver.class);
        assertNull(clzResolverBean);

        clzResolverBean = TestSpringContext.getBean("ClzResolver");
        assertNull(clzResolverBean);

        var multiBean = TestSpringContext.getBean(TestMultiClass.class);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("TestMultiClassA", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("AAA", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassB", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("BBB", multiBean.getName());
    }
}
