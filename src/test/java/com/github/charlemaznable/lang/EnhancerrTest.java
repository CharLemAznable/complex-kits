package com.github.charlemaznable.lang;

import lombok.val;
import lombok.var;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnhancerrTest {

    @Test
    public void testEnhancerrCreate() {
        var actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Interceptor(), new Object[]{});
        actual.method();
        assertEquals(1, Interceptor.count);

        actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Class[]{},
                new Interceptor(), new Object[]{});
        actual.method();
        assertEquals(2, Interceptor.count);

        actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Class[]{}, null,
                new Callback[]{new Interceptor()}, new Object[]{});
        actual.method();
        assertEquals(3, Interceptor.count);

        val params = new Object[]{new ActualParamType()};

        actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Interceptor(), params);
        actual.method();
        assertEquals(4, Interceptor.count);

        actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Class[]{},
                new Interceptor(), params);
        actual.method();
        assertEquals(5, Interceptor.count);

        actual = (ActualClass) Enhancerr.create(
                ActualClass.class, new Class[]{}, null,
                new Callback[]{new Interceptor()}, params);
        actual.method();
        assertEquals(6, Interceptor.count);
    }

    static class ActualClass {

        public ActualClass() {
        }

        public ActualClass(ParamType init) {
        }

        public void method() {
        }
    }

    static class ParamType {
    }

    static class ActualParamType extends ParamType {
    }

    static class Interceptor implements MethodInterceptor {

        static int count = 0;

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            count++;
            return methodProxy.invokeSuper(o, args);
        }
    }
}
