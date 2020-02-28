package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.miner.MinerInjector;
import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.core.miner.MinerFactory.springMinerLoader;

public class TestSampleUrlProviderContext implements UrlProvider {

    private TestDefaultContext current;
    private TestDefaultContext spring;
    private TestDefaultContext guice;

    public TestSampleUrlProviderContext() {
        this.current = getMiner(TestDefaultContext.class);
        this.spring = springMinerLoader().getMiner(TestDefaultContext.class);
        this.guice = new MinerInjector().getMiner(TestDefaultContext.class);
    }

    @Override
    public String url(Class<?> clazz, Method method) {
        return "/" + current.thread() + "-" + spring.thread() + "-" + guice.thread();
    }
}
