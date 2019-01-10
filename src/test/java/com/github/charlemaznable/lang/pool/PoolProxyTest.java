package com.github.charlemaznable.lang.pool;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PoolProxyTest {

    @Test
    public void testPoolProxy() {
        TestPoolProxyObject testPoolProxyObject = PoolProxy.create(
                new PooledObjectCreator<TestPoolProxyObject>() {

                    @Override
                    public TestPoolProxyObject create() {
                        return new TestPoolProxyObject();
                    }
                }, new PoolProxyConfigBuilder().maxTotal(1000).build()
        );
        assertEquals("proxy invoked", testPoolProxyObject.invoke());
    }

    @NoArgsConstructor
    private static class TestPoolProxyObject {

        public Object invoke(Object... args) {
            return "proxy invoked";
        }
    }
}
