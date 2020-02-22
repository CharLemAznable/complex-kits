package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testClass.TestAnnotation;
import com.github.charlemaznable.core.spring.testClass.TestClass;
import com.github.charlemaznable.core.spring.testClass.TestClassA;
import com.github.charlemaznable.core.spring.testClass.TestClassB;
import com.github.charlemaznable.core.spring.testClass.TestClassC;
import com.github.charlemaznable.core.spring.testClass.TestClassD;
import com.github.charlemaznable.core.spring.testClass.TestClassE;
import com.github.charlemaznable.core.spring.testClass.TestClassF;
import com.github.charlemaznable.core.spring.testClass.TestClassG;
import com.github.charlemaznable.core.spring.testClass.TestSpringContext;
import lombok.var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SpringContextErrorTest {

    @Test
    public void testSpringContextError() {
        var multiBeanNames = SpringContext.getBeanNamesForType(TestClass.class);
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

        TestClassB testB = TestSpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = TestSpringContext.getBeanOrReflect(TestClassB.class);
        assertNotNull(testB);
        assertNull(testB.testClass);
        testB = TestSpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = TestSpringContext.getBeanOrCreate(TestClassB.class);
        assertNull(testB);
        testB = TestSpringContext.getBean(TestClassB.class);
        assertNull(testB);

        TestClassC testC = TestSpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = TestSpringContext.getBean(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNull(testC.testClass);
        testC = TestSpringContext.getBean(TestClassC.class);
        assertNull(testC);
        testC = TestSpringContext.getBeanOrAutowire(TestClassC.class, new TestClassC());
        assertNotNull(testC);
        assertNull(testC.testClass);
        testC = TestSpringContext.getBean(TestClassC.class);
        assertNull(testC);

        TestClassD testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = TestSpringContext.getBeanOrReflect("TestClassD", TestClassD.class);
        assertNotNull(testD);
        assertNull(testD.testClass);
        testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = TestSpringContext.getBeanOrCreate("TestClassD", TestClassD.class);
        assertNull(testD);
        testD = TestSpringContext.getBean("TestClassD", TestClassD.class);
        assertNull(testD);

        TestClassE testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNull(testE.testClass);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);
        testE = TestSpringContext.getBeanOrAutowire("TestClassE", TestClassE.class, new TestClassE());
        assertNotNull(testE);
        assertNull(testE.testClass);
        testE = TestSpringContext.getBean("TestClassE", TestClassE.class);
        assertNull(testE);

        TestClassF testF = TestSpringContext.getBean(TestClassF.class);
        assertNull(testF);
        testF = TestSpringContext.createBean(TestClassF.class);
        assertNull(testF);
        testF = TestSpringContext.getBean(TestClassF.class);
        assertNull(testF);

        TestClassG testG = TestSpringContext.getBean(TestClassG.class);
        assertNull(testG);
        testG = TestSpringContext.autowireBean(new TestClassG());
        assertNotNull(testG);
        assertNull(testG.testClass);
        testG = TestSpringContext.getBean(TestClassG.class);
        assertNull(testG);
    }
}
