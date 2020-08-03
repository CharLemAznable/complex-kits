package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testcontext.TestAnnotation;
import com.github.charlemaznable.core.spring.testcontext.TestClass;
import com.github.charlemaznable.core.spring.testcontext.TestClassA;
import com.github.charlemaznable.core.spring.testcontext.TestClassB;
import com.github.charlemaznable.core.spring.testcontext.TestClassC;
import com.github.charlemaznable.core.spring.testcontext.TestClassD;
import com.github.charlemaznable.core.spring.testcontext.TestClassE;
import com.github.charlemaznable.core.spring.testcontext.TestClassF;
import com.github.charlemaznable.core.spring.testcontext.TestClassG;
import com.github.charlemaznable.core.spring.testcontext.TestConfiguration;
import com.github.charlemaznable.core.spring.testcontext.TestMultiClass;
import com.github.charlemaznable.core.spring.testcontext.TestRecreateBaseClass;
import com.github.charlemaznable.core.spring.testcontext.TestRecreateSubClass;
import com.github.charlemaznable.core.spring.testcontext.TestRewireBaseClass;
import com.github.charlemaznable.core.spring.testcontext.TestRewireSubClass;
import com.github.charlemaznable.core.spring.testcontext.TestSpringContext;
import com.github.charlemaznable.core.spring.testcontext.TestSubSpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class SpringContextTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSpringContext() {
        TestClass testClassBean = TestSpringContext.getBean(TestClass.class);
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean((Class) null, new TestClass());
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean("TestClass");
        assertNotNull(testClassBean);

        testClassBean = TestSpringContext.getBean("", new TestClass());
        assertNotNull(testClassBean);

        ClzResolver clzResolverBean = TestSpringContext.getBean(ClzResolver.class);
        assertNull(clzResolverBean);

        clzResolverBean = TestSpringContext.getBean("ClzResolver");
        assertNull(clzResolverBean);

        TestMultiClass multiBean = TestSpringContext.getBean(TestMultiClass.class);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("", (Class<TestMultiClass>) null);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("", TestMultiClass.class);
        assertNull(multiBean);

        multiBean = TestSpringContext.getBean("TestMultiClassA", (Class<TestMultiClass>) null);
        assertNotNull(multiBean);
        assertEquals("AAA", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassB", (Class<TestMultiClass>) null);
        assertNotNull(multiBean);
        assertEquals("BBB", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassA", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("AAA", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassB", TestMultiClass.class);
        assertNotNull(multiBean);
        assertEquals("BBB", multiBean.getName());

        multiBean = TestSpringContext.getBean("TestMultiClassC", TestMultiClass.class);
        assertNull(multiBean);

        String[] multiBeanNames = TestSpringContext.getBeanNamesForType(null);
        assertEquals(0, multiBeanNames.length);
        multiBeanNames = TestSpringContext.getBeanNamesForType(TestClass.class);
        assertEquals(1, multiBeanNames.length);
        assertEquals("TestClass", multiBeanNames[0]);
        multiBeanNames = TestSpringContext.getBeanNamesForType(TestMultiClass.class);
        assertEquals(2, multiBeanNames.length);
        Arrays.sort(multiBeanNames);
        assertEquals("TestMultiClassA", multiBeanNames[0]);
        assertEquals("TestMultiClassB", multiBeanNames[1]);

        multiBeanNames = TestSpringContext.getBeanNamesForAnnotation(null);
        assertEquals(0, multiBeanNames.length);
        multiBeanNames = TestSpringContext.getBeanNamesForAnnotation(TestAnnotation.class);
        Arrays.sort(multiBeanNames);
        assertEquals(2, multiBeanNames.length);
        assertEquals(TestSpringContext.class.getName(), multiBeanNames[0]);
        assertEquals(TestSubSpringContext.class.getName(), multiBeanNames[1]);
    }

    @Test
    public void testSpringContextDefault() {
        TestClassA testA = TestSpringContext.getBean("TestClassA");
        assertNull(testA);
        testA = TestSpringContext.getBean("TestClassA", new TestClassA());
        assertNotNull(testA);
        assertNull(testA.testClass);
        testA = TestSpringContext.getBean("TestClassA");
        assertNull(testA);
        testA = TestSpringContext.getBeanOrAutowire("TestClassA", new TestClassA());
        assertNotNull(testA);
        assertNotNull(testA.testClass);
        testA = TestSpringContext.getBean("TestClassA");
        assertNotNull(testA);
        assertNotNull(testA.testClass);

        TestClassB testB = TestSpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = TestSpringContext.getBeanOrReflect(TestClassB.class);
        assertNotNull(testB);
        assertNull(testB.testClass);
        testB = TestSpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = TestSpringContext.getBeanOrCreate(TestClassB.class);
        assertNotNull(testB);
        assertNotNull(testB.testClass);
        testB = TestSpringContext.getBean(TestClassB.class);
        assertNotNull(testB);
        assertNotNull(testB.testClass);

        TestClassC testC = TestSpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = TestSpringContext.getBean(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNull(testC.testClass);
        testC = TestSpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = TestSpringContext.getBeanOrAutowire(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNotNull(testC.testClass);
        testC = TestSpringContext.getBean(TestClassC.class);
        assertNotNull(testC);
        assertNotNull(testC.testClass);

        TestClassD testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = TestSpringContext.getBeanOrReflect("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNull(testD.testClass);
        testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = TestSpringContext.getBeanOrCreate("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNotNull(testD.testClass);
        testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNotNull(testD.testClass);

        TestClassE testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNull(testE.testClass);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = TestSpringContext.getBeanOrAutowire("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNotNull(testE.testClass);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNotNull(testE);
        assertNotNull(testE.testClass);

        TestClassF testF = TestSpringContext.getBean(TestClassF.class);
        assertNull(testF);
        testF = TestSpringContext.createBean(TestClassF.class);
        assertNotNull(testF);
        assertNotNull(testF.testClass);
        testF = TestSpringContext.getBean(TestClassF.class);
        assertNotNull(testF);
        assertNotNull(testF.testClass);

        TestClassG testG = TestSpringContext.getBean(TestClassG.class);
        assertNull(testG);
        testG = TestSpringContext.autowireBean(new TestClassG());
        assertNotNull(testG);
        assertNotNull(testG.testClass);
        testG = TestSpringContext.getBean(TestClassG.class);
        assertNotNull(testG);
        assertNotNull(testG.testClass);

        assertNull(TestSpringContext.createBean(null));
        assertNull(TestSpringContext.autowireBean(null));
    }

    @Test
    public void testSpringContextRecreate() {
        onClass(TestSpringContext.class).field("defaultListableBeanFactory")
                .set("allowBeanDefinitionOverriding", false);

        TestRecreateBaseClass base = TestSpringContext.getBeanOrCreate(TestRecreateBaseClass.class);
        assertNotNull(base);

        TestRecreateSubClass sub = TestSpringContext.getBeanOrCreate(TestRecreateSubClass.class);
        assertNotNull(sub);
        assertNotEquals(base, sub);

        TestRecreateBaseClass base2 = TestSpringContext.getBeanOrCreate(TestRecreateBaseClass.class);
        assertNotNull(base2);
        assertEquals(base, base2);

        onClass(TestSpringContext.class).field("defaultListableBeanFactory")
                .set("allowBeanDefinitionOverriding", true);
    }

    @Test
    public void testSpringContextRewire() {
        onClass(TestSpringContext.class).field("defaultListableBeanFactory")
                .set("allowBeanDefinitionOverriding", false);

        val baseBean = new TestRewireBaseClass();
        TestRewireBaseClass base = TestSpringContext.getBeanOrAutowire(TestRewireBaseClass.class, baseBean);
        assertNotNull(base);
        assertEquals(baseBean, base);

        val subBean = new TestRewireSubClass();
        TestRewireSubClass sub = TestSpringContext.getBeanOrAutowire(TestRewireSubClass.class, subBean);
        assertNotNull(sub);
        assertNotEquals(base, sub);
        assertEquals(subBean, sub);

        val baseBean2 = new TestRewireBaseClass();
        TestRewireBaseClass base2 = TestSpringContext.getBeanOrAutowire(TestRewireBaseClass.class, baseBean2);
        assertNotNull(base2);
        assertEquals(base, base2);
        assertNotEquals(baseBean2, base2);

        onClass(TestSpringContext.class).field("defaultListableBeanFactory")
                .set("allowBeanDefinitionOverriding", true);
    }
}
