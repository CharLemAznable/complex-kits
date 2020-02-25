package com.github.charlemaznable.core.spring.testcontext;

import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestClassB {

    @Autowired
    public TestClass testClass;
}
