package com.github.charlemaznable.core.net.ohclient.testscan;

import com.github.charlemaznable.core.net.ohclient.OhClient;
import com.github.charlemaznable.core.net.common.Mapping;

@TestClientMapping
@OhClient
public interface TestHttpClient {

    @Mapping(urlProvider = TestSampleUrlProvider.class)
    String sample();

    default String sampleWrapper() {
        return "{" + sample() + "}";
    }
}
