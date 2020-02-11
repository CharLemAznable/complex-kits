package com.github.charlemaznable.core.net.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.Proxy;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientProxy {

    String ip() default "";

    int port() default 80;

    Class<? extends ProxyProvider> proxyProvider() default ProxyProvider.class;

    interface ProxyProvider {

        default Proxy proxy(Class<?> clazz) {
            throw new ProviderException(this.getClass().getName()
                    + "#proxy(Class<?>) need be overwritten");
        }

        default Proxy proxy(Class<?> clazz, Method method) {
            throw new ProviderException(this.getClass().getName()
                    + "#proxy(Class<?>, Method) need be overwritten");
        }
    }
}
