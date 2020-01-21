package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhConfigProxy;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigProxy.ProxyProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigProxyDisabled;
import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.joor.Reflect.on;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("UnusedReturnValue")
public class ProxyProviderTest {

    @Test
    public void testProxyPlain() {
        val httpClient = getClient(ProxyPlainHttpClient.class);
        val callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        val address = (InetSocketAddress) checkNotNull(okHttpClient.proxy()).address();
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());
        assertEquals(41111, address.getPort());
    }

    @Test
    public void testProxyProvider() {
        val httpClient = getClient(ProxyProviderHttpClient.class);
        val callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        val address = (InetSocketAddress) checkNotNull(okHttpClient.proxy()).address();
        assertEquals("127.0.0.1", address.getAddress().getHostAddress());
        assertEquals(41113, address.getPort());
    }

    @SneakyThrows
    @Test
    public void testProxyParam() {
        val httpClient = getClient(ProxyParamHttpClient.class);
        val proxyParam = new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 41115));
        try {
            httpClient.sample(proxyParam);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41115", e.getMessage());
        }
        try {
            httpClient.sample(null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41114", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testMethodProxy() {
        val httpClient = getClient(MethodProxyHttpClient.class);
        try {
            httpClient.sampleDefault();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41117", e.getMessage());
        }
        try {
            httpClient.samplePlain();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41118", e.getMessage());
        }
        try {
            httpClient.sampleProvider();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41118", e.getMessage());
        }
        try {
            httpClient.sampleDisabled();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41116", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testErrorProxy() {
        assertThrows(OhException.class, () ->
                getClient(ErrorProxyHttpClient1.class));

        val httpClient = getClient(ErrorProxyHttpClient2.class);
        assertThrows(OhException.class, httpClient::sample);
    }

    @OhClient("${root}:41110")
    @OhConfigProxy(ip = "127.0.0.1", port = 41111)
    public interface ProxyPlainHttpClient {

        String sample();
    }

    @OhClient("${root}:41112")
    @OhConfigProxy(proxyProvider = TestProxyProvider.class)
    public interface ProxyProviderHttpClient {

        String sample();
    }

    @OhClient("${root}:41114")
    @OhConfigProxy(ip = "127.0.0.1", port = 41115)
    public interface ProxyParamHttpClient {

        String sample(Proxy proxy);
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @OhConfigProxyDisabled
    public @interface Disabled {}

    @OhClient("${root}:41116")
    @OhConfigProxy(proxyProvider = MethodProxyProvider.class)
    public interface MethodProxyHttpClient {

        String sampleDefault();

        @OhConfigProxy(ip = "127.0.0.1", port = 41118)
        String samplePlain();

        @OhConfigProxy(proxyProvider = MethodProxyProvider.class)
        String sampleProvider();

        @Disabled
        String sampleDisabled();
    }

    @OhClient("${root}:41119")
    @OhConfigProxy(proxyProvider = ErrorProxyProvider.class)
    public interface ErrorProxyHttpClient1 {}

    @OhClient("${root}:41119")
    @OhConfigProxy(proxyProvider = NoErrorProxyProvider.class)
    public interface ErrorProxyHttpClient2 {

        @OhConfigProxy(proxyProvider = ErrorProxyProvider.class)
        String sample();
    }

    public static class TestProxyProvider implements ProxyProvider {

        @Override
        public Proxy proxy(Class<?> clazz) {
            return new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 41113));
        }
    }

    public static class MethodProxyProvider implements ProxyProvider {

        @Override
        public Proxy proxy(Class<?> clazz) {
            return new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 41117));
        }

        @Override
        public Proxy proxy(Class<?> clazz, Method method) {
            return new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 41118));
        }
    }

    public static class ErrorProxyProvider implements ProxyProvider {}

    public static class NoErrorProxyProvider implements ProxyProvider {

        @Override
        public Proxy proxy(Class<?> clazz) {
            return new Proxy(Type.HTTP, new InetSocketAddress("192.168.0.11", 41110));
        }
    }
}
