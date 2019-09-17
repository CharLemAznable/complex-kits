package com.github.charlemaznable.core.spring.testClass;

import com.github.charlemaznable.core.spring.ComplexBeanNameGenerator;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(nameGenerator = ComplexBeanNameGenerator.class)
public class TestConfiguration {

    @Bean("TestClass")
    public TestClass testClass() {
        return new TestClass();
    }

    @Bean("TestMultiClassA")
    public TestMultiClass testMultiClassA() {
        val instance = new TestMultiClass();
        instance.setName("AAA");
        return instance;
    }

    @Bean("TestMultiClassB")
    public TestMultiClass testMultiClassB() {
        val instance = new TestMultiClass();
        instance.setName("BBB");
        return instance;
    }
}
