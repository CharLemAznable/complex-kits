package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestSampleWrapUrlProvider implements UrlProvider {

    @Autowired
    private TestSampleUrlProvider provider;

    @Override
    public String url(Class<?> clazz, Method method) {
        return provider.sample();
    }
}
