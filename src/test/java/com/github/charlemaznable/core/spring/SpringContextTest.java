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

import java.util.Arrays;

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

        multiBean = TestSpringContext.getBean("", (Class<TestMultiClass>) null);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("", TestMultiClass.class);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("TestMultiClassA", (Class<TestMultiClass>) null);
        assertNotNull(multiBean);
        assertEquals("AAA", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassA", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("AAA", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassB", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("BBB", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassC", TestMultiClass.class);
        assertNull(multiBean);

        var multiBeanNames = TestSpringContext.getBeanNamesForType(null);
        assertEquals(0, multiBeanNames.length);
        multiBeanNames = TestSpringContext.getBeanNamesForType(TestClass.class);
        assertEquals(1, multiBeanNames.length);
        assertEquals("TestClass", multiBeanNames[0]);
        multiBeanNames = TestSpringContext.getBeanNamesForType(TestMultiClass.class);
        assertEquals(2, multiBeanNames.length);
        Arrays.sort(multiBeanNames);
        assertEquals("TestMultiClassA", multiBeanNames[0]);
        assertEquals("TestMultiClassB", multiBeanNames[1]);
    }
}
