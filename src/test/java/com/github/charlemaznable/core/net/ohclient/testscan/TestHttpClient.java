package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.ohclient.OhClient;

@TestClientMapping
@OhClient
public interface TestHttpClient {

    @Mapping(urlProvider = TestSampleUrlProvider.class)
    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }

    @Mapping(urlProvider = TestSampleWrapUrlProvider.class)
    String sampleWrap();
}
