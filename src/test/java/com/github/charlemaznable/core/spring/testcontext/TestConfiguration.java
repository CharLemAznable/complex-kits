package com.github.charlemaznable.core.spring.testcontext;

import com.github.charlemaznable.core.spring.ComplexComponentScan;
import lombok.val;
import org.springframework.context.annotation.Bean;

@ComplexComponentScan
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
