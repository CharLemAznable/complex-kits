package com.github.charlemaznable.core.miner.testClass;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class TestMinerDataIdImpl implements TestMinerDataId {

    @Override
    public String dataId(Class<?> minerClass, Method method) {
        return "long";
    }
}
