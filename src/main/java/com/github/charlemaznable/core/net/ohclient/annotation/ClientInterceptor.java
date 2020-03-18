package com.github.charlemaznable.core.net.ohclient.annotation;

import com.github.charlemaznable.core.net.common.ProviderException;
import okhttp3.Interceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ClientInterceptors.class)
public @interface ClientInterceptor {

    Class<? extends Interceptor> value() default Interceptor.class;

    Class<? extends InterceptorProvider> provider() default InterceptorProvider.class;

    interface InterceptorProvider {

        default Interceptor interceptor(Class<?> clazz) {
            throw new ProviderException(this.getClass().getName()
                    + "#interceptor(Class<?>) need be overwritten");
        }

        default Interceptor interceptor(Class<?> clazz, Method method) {
            throw new ProviderException(this.getClass().getName()
                    + "#interceptor(Class<?>, Method) need be overwritten");
        }
    }
}
