package com.github.charlemaznable.core.spring.testClass;

import com.github.charlemaznable.core.spring.ComplexBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(nameGenerator = ComplexBeanNameGenerator.class)
public class TestConfiguration {

    @Bean("TestClass")
    public TestClass testClass() {
        return new TestClass();
    }
}
