package com.github.charlemaznable.core.miner.testminer;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.context.FactoryContext.SpringFactory;
import com.github.charlemaznable.core.guice.InjectorFactory;
import com.github.charlemaznable.core.miner.MinerConfig.DefaultValueProvider;

import java.lang.reflect.Method;

public class TestDefaultMinerProvider implements DefaultValueProvider {

    private String init;

    public TestDefaultMinerProvider() {
        if (FactoryContext.get() instanceof SpringFactory) {
            init = "spring";
        } else if (FactoryContext.get() instanceof InjectorFactory) {
            init = "guice";
        } else init = "";
    }

    @Override
    public String defaultValue(Class<?> minerClass, Method method) {
        if (FactoryContext.get() instanceof SpringFactory) {
            return init + "spring";
        } else if (FactoryContext.get() instanceof InjectorFactory) {
            return init + "guice";
        } else return init;
    }
}
