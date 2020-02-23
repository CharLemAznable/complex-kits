package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.checkNull;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestSampleWrapUrlProvider implements UrlProvider {

    @Inject
    @Autowired
    private TestSampleUrlProvider provider;

    @Override
    public String url(Class<?> clazz, Method method) {
        return checkNull(provider, () -> "sampleError", TestSampleUrlProvider::sample);
    }
}
