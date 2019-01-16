package com.github.charlemaznable.spring.testClass;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class TestConfiguration {

    @Bean("TestClass")
    public TestClass testClass() {
        return new TestClass();
    }
}
