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
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SpringContextErrorTest {

    @Test
    public void testSpringContextError() {
        val SpringContextClass = onClass(SpringContext.class);
        val applicationContext = SpringContextClass.field("applicationContext").get();
        SpringContextClass.set("applicationContext", null);

        String[] multiBeanNames = SpringContext.getBeanNamesForType(TestClass.class);
        assertEquals(0, multiBeanNames.length);
        multiBeanNames = SpringContext.getBeanNamesForAnnotation(TestAnnotation.class);
        assertEquals(0, multiBeanNames.length);

        TestClassA testA = SpringContext.getBean("TestClassA");
        assertNull(testA);
        testA = SpringContext.getBean("TestClassA", new TestClassA());
        assertNotNull(testA);
        assertNull(testA.testClass);
        testA = SpringContext.getBean("TestClassA");
        assertNull(testA);
        testA = SpringContext.getBeanOrAutowire("TestClassA", new TestClassA());
        assertNotNull(testA);
        assertNull(testA.testClass);
        testA = SpringContext.getBean("TestClassA");
        assertNull(testA);

        TestClassB testB = SpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = SpringContext.getBeanOrReflect(TestClassB.class);
        assertNotNull(testB);
        assertNull(testB.testClass);
        testB = SpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = SpringContext.getBeanOrCreate(TestClassB.class);
        assertNotNull(testB);
        assertNull(testB.testClass);
        testB = SpringContext.getBean(TestClassB.class);
        assertNull(testB);

        TestClassC testC = SpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = SpringContext.getBean(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNull(testC.testClass);
        testC = SpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = SpringContext.getBeanOrAutowire(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNull(testC.testClass);
        testC = SpringContext.getBean(TestClassC.class);
        assertNull(testC);

        TestClassD testD = SpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = SpringContext.getBeanOrReflect("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNull(testD.testClass);
        testD = SpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = SpringContext.getBeanOrCreate("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNull(testD.testClass);
        testD = SpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);

        TestClassE testE = SpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = SpringContext.getBean("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNull(testE.testClass);
        testE = SpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = SpringContext.getBeanOrAutowire("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNull(testE.testClass);
        testE = SpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);

        TestClassF testF = SpringContext.getBean(TestClassF.class);
        assertNull(testF);
        testF = SpringContext.createBean(TestClassF.class);
        assertNotNull(testF);
        assertNull(testF.testClass);
        testF = SpringContext.getBean(TestClassF.class);
        assertNull(testF);

        TestClassG testG = SpringContext.getBean(TestClassG.class);
        assertNull(testG);
        testG = SpringContext.autowireBean(new TestClassG());
        assertNotNull(testG);
        assertNull(testG.testClass);
        testG = SpringContext.getBean(TestClassG.class);
        assertNull(testG);

        SpringContextClass.set("applicationContext", applicationContext);
    }
}
