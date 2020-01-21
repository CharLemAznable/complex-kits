package com.github.charlemaznable.core.net.ohclient.config;

import com.github.charlemaznable.core.net.ohclient.exception.OhException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OhConfigSSL {

    Class<? extends SSLSocketFactoryProvider> sslSocketFactoryProvider()
            default SSLSocketFactoryProvider.class;

    Class<? extends X509TrustManagerProvider> x509TrustManagerProvider()
            default X509TrustManagerProvider.class;

    Class<? extends HostnameVerifierProvider> hostnameVerifierProvider()
            default HostnameVerifierProvider.class;

    interface SSLSocketFactoryProvider {

        default SSLSocketFactory sslSocketFactory(Class<?> clazz) {
            throw new OhException(this.getClass().getName()
                    + "#sslSocketFactory(Class<?>) need be overwritten");
        }

        default SSLSocketFactory sslSocketFactory(Class<?> clazz, Method method) {
            throw new OhException(this.getClass().getName()
                    + "#sslSocketFactory(Class<?>, Method) need be overwritten");
        }
    }

    interface X509TrustManagerProvider {

        default X509TrustManager x509TrustManager(Class<?> clazz) {
            throw new OhException(this.getClass().getName()
                    + "#x509TrustManager(Class<?>) need be overwritten");
        }

        default X509TrustManager x509TrustManager(Class<?> clazz, Method method) {
            throw new OhException(this.getClass().getName()
                    + "#x509TrustManager(Class<?>, Method) need be overwritten");
        }
    }

    interface HostnameVerifierProvider {

        default HostnameVerifier hostnameVerifier(Class<?> clazz) {
            throw new OhException(this.getClass().getName()
                    + "#hostnameVerifier(Class<?>) need be overwritten");
        }

        default HostnameVerifier hostnameVerifier(Class<?> clazz, Method method) {
            throw new OhException(this.getClass().getName()
                    + "#hostnameVerifier(Class<?>, Method) need be overwritten");
        }
    }
}
