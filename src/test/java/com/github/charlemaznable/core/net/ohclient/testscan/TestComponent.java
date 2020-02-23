package com.github.charlemaznable.core.net.ohclient.testscan;

import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestComponent {

    @Inject
    @Autowired(required = false)
    public TestHttpClient testHttpClient;
}
