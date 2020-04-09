package com.github.charlemaznable.core.net.ohclient.testclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.context.FactoryContext.SpringFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class TestComponentSpring {

    @Getter
    private TestHttpClient testHttpClient;

    @Autowired
    public TestComponentSpring(@Nullable TestHttpClient testHttpClient) {
        assertTrue(FactoryContext.get() instanceof SpringFactory);
        this.testHttpClient = nullThen(testHttpClient, () -> getClient(TestHttpClient.class));
    }
}
