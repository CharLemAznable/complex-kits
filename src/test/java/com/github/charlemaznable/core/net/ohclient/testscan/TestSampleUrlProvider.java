package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.miner.MinerConfig;
import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;

import java.lang.reflect.Method;

@MinerConfig("DEFAULT_DATA")
public interface TestSampleUrlProvider extends UrlProvider {

    @MinerConfig(defaultValue = "sample")
    String sample();

    @Override
    default String url(Class<?> clazz, Method method) {
        return sample();
    }
}
