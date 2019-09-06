package com.github.charlemaznable.core.spring;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan(nameGenerator = ComplexBeanNameGenerator.class)
public class ComplexComponentScan {
}
