package com.github.charlemaznable.core.miner.testminer;

import com.github.charlemaznable.core.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.core.miner.MinerModular;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.core.miner.MinerFactory.springMinerLoader;

public class TestDefaultInContext implements DefaultValueProvider {

    private TestDefaultMiner current;
    private TestDefaultMiner spring;
    private TestDefaultMiner guice;

    public TestDefaultInContext() {
        this.current = getMiner(TestDefaultMiner.class);
        this.spring = springMinerLoader().getMiner(TestDefaultMiner.class);
        this.guice = new MinerModular().getMiner(TestDefaultMiner.class);
    }

    @Override
    public String defaultValue(Class<?> minerClass, Method method) {
        return current.thread() + "&" + spring.thread() + "&" + guice.thread();
    }
}
