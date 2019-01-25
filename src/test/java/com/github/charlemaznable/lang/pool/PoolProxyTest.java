package com.github.charlemaznable.lang.pool;

import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PoolProxyTest {

    @Test
    public void testPoolProxy() {
        val testPoolProxyObject = PoolProxy.create(
                new PooledObjectCreator<TestPoolProxyObject>() {

                    @Override
                    public TestPoolProxyObject create() {
                        return new TestPoolProxyObject();
                    }
                }, new PoolProxyConfigBuilder().maxTotal(1000).maxIdle(10).minIdle(0).build()
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
