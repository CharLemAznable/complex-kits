package com.github.charlemaznable.core.net.ohclient.testclient;

import com.google.inject.Inject;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestComponent {

    @Getter
    private TestHttpClient testHttpClient;

    @Inject
    @Autowired
    public TestComponent(TestHttpClient testHttpClient) {
        this.testHttpClient = testHttpClient;
    }
}
