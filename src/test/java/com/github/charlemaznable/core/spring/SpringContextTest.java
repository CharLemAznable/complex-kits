package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testClass.TestClass;
import com.github.charlemaznable.core.spring.testClass.TestConfiguration;
import com.github.charlemaznable.core.spring.testClass.TestCreateClassA;
import com.github.charlemaznable.core.spring.testClass.TestCreateClassB;
import com.github.charlemaznable.core.spring.testClass.TestCreateClassC;
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

    @SuppressWarnings("unchecked")
    @Test
    public void testSpringContext() {
        var testClassBean = TestSpringContext.getBean(TestClass.class);
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean((Class) null, new TestClass());
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean("TestClass");
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean("", new TestClass());
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

    @Test
    public void testSpringContextCreate() {
        var createBeanA = TestSpringContext.getBean(TestCreateClassA.class);
        assertNull(createBeanA);
        createBeanA = TestSpringContext.getBeanOrReflect(TestCreateClassA.class);
        assertNotNull(createBeanA);
        createBeanA = TestSpringContext.getBean(TestCreateClassA.class);
        assertNull(createBeanA);
        createBeanA = TestSpringContext.getBeanOrCreate(TestCreateClassA.class);
        assertNotNull(createBeanA);
        createBeanA = TestSpringContext.getBean(TestCreateClassA.class);
        assertNotNull(createBeanA);
        createBeanA = TestSpringContext.getBean(TestCreateClassA.class.getName());
        assertNotNull(createBeanA);
        createBeanA = TestSpringContext.getBean(TestCreateClassA.class.getName(), TestCreateClassA.class);
        assertNotNull(createBeanA);

        var createBeanB = TestSpringContext.getBean("TestCreateClassB", TestCreateClassB.class);
        assertNull(createBeanB);
        createBeanB = TestSpringContext.getBeanOrReflect("TestCreateClassB", TestCreateClassB.class);
        assertNotNull(createBeanB);
        createBeanB = TestSpringContext.getBean("TestCreateClassB", TestCreateClassB.class);
        assertNull(createBeanB);
        createBeanB = TestSpringContext.getBeanOrCreate("TestCreateClassB", TestCreateClassB.class);
        assertNotNull(createBeanB);
        createBeanB = TestSpringContext.getBean(TestCreateClassB.class);
        assertNotNull(createBeanB);
        createBeanB = TestSpringContext.getBean("TestCreateClassB");
        assertNotNull(createBeanB);
        createBeanB = TestSpringContext.getBean("TestCreateClassB", TestCreateClassB.class);
        assertNotNull(createBeanB);

        var createBeanC = TestSpringContext.getBean(TestCreateClassC.class);
        assertNull(createBeanC);
        createBeanC = TestSpringContext.getBean(TestCreateClassC.class.getName());
        assertNull(createBeanC);
        createBeanC = TestSpringContext.getBean(TestCreateClassC.class.getName(), TestCreateClassC.class);
        assertNull(createBeanC);

        TestSpringContext.autowireBean(new TestCreateClassC());

        createBeanC = TestSpringContext.getBean(TestCreateClassC.class);
        assertNotNull(createBeanC);
        createBeanC = TestSpringContext.getBean(TestCreateClassC.class.getName());
        assertNotNull(createBeanC);
        createBeanC = TestSpringContext.getBean(TestCreateClassC.class.getName(), TestCreateClassC.class);
        assertNotNull(createBeanC);

        assertNotNull(createBeanC.testClass);
        assertEquals(TestClass.class, createBeanC.testClass.getClass());
    }
}
