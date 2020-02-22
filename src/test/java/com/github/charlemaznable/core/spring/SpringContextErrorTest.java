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
import com.github.charlemaznable.core.spring.testClass.TestClassH;
import com.github.charlemaznable.core.spring.testClass.TestClassI;
import lombok.val;
import lombok.var;
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

        TestClassB testB = SpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = SpringContext.getBeanOrReflect(TestClassB.class);
        assertNotNull(testB);
        assertNull(testB.testClass);
        testB = SpringContext.getBean(TestClassB.class);
        assertNull(testB);
        testB = SpringContext.getBeanOrCreate(TestClassB.class);
        assertNull(testB);
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
        assertNull(testD);
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
        assertNull(testF);
        testF = SpringContext.getBean(TestClassF.class);
        assertNull(testF);

        TestClassG testG = SpringContext.getBean(TestClassG.class);
        assertNull(testG);
        testG = SpringContext.autowireBean(new TestClassG());
        assertNotNull(testG);
        assertNull(testG.testClass);
        testG = SpringContext.getBean(TestClassG.class);
        assertNull(testG);

        TestClassH testH = SpringContext.getBean(TestClassH.class);
        assertNull(testH);
        testH = SpringContext.getBeanOrReflectAutowire(TestClassH.class);
        assertNotNull(testH);
        assertNull(testH.testClass);
        testH = SpringContext.getBean(TestClassH.class);
        assertNull(testH);

        TestClassI testI = SpringContext.getBean("TestClassI", TestClassI.class);
        assertNull(testI);
        testI = SpringContext.getBeanOrReflectAutowire("TestClassI", TestClassI.class);
        assertNotNull(testI);
        assertNull(testI.testClass);
        testI = SpringContext.getBean("TestClassI", TestClassI.class);
        assertNull(testI);

        SpringContextClass.set("applicationContext", applicationContext);
    }
}
