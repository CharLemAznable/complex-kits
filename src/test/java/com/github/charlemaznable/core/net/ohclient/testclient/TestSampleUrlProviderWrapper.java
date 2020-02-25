package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestSampleUrlProviderWrapper implements UrlProvider {

    private TestSampleUrlProvider provider;

    public TestSampleUrlProviderWrapper() {
        this(null);
    }

    @Inject
    @Autowired
    public TestSampleUrlProviderWrapper(@Nullable TestSampleUrlProvider provider) {
        this.provider = nullThen(provider, () -> () -> "sampleError");
    }

    @Override
    public String url(Class<?> clazz, Method method) {
        return this.provider.url(clazz, method);
    }
}
