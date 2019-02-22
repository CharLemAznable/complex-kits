package com.github.charlemaznable.lang.pool;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PoolProxyTest {

    @Test
    public void testPoolProxy() {
        new PoolProxy();

        val poolConfig = new PoolProxyConfigBuilder()
                .maxTotal(8).maxIdle(8).minIdle(0).<TestPoolProxyObject>build();

        var testPoolProxyObject = PoolProxy.builder(
                new PooledObjectCreator<TestPoolProxyObject>() {}).build();
        assertEquals("proxy invoked", testPoolProxyObject.invoke());

        testPoolProxyObject = PoolProxy.builder(
                new PooledObjectCreator<TestPoolProxyObject>() {}).config(poolConfig).args("ARGUMENT").build();
        assertEquals("proxy invoked: ARGUMENT", testPoolProxyObject.invokeArgument());

        assertThrows(IllegalArgumentException.class, () -> {
            try {
                PoolProxy.builder(
                        new PooledObjectCreator<TestPoolProxyObject>() {

                            @Override
                            public TestPoolProxyObject create(Object... args) {
                                return new TestPoolProxyObject(args[0].toString());
                            }
                        }).args("ARGUMENT", "ILLEGAL").build();
            } catch (Exception e) {
                assertEquals("Constructor with such arguments Not Found", e.getMessage());
                throw e;
            }
        });

        assertThrows(ReflectException.class, () -> PoolProxy.builder(
                new PooledObjectCreator<TestPoolProxyObject>() {}).args("ARGUMENT", "ILLEGAL").build());
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    static class TestPoolProxyObject {

        private String argument;

        public Object invoke(Object... args) {
            return "proxy invoked";
        }

        public Object invokeArgument(Object... args) {
            return "proxy invoked: " + argument;
        }
    }
}
