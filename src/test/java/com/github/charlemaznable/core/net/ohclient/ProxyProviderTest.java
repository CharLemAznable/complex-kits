package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.ProviderException;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxy.ProxyProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientProxyDisabled;
import lombok.SneakyThrows;
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

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.joor.Reflect.on;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("UnusedReturnValue")
public class ProxyProviderTest {

    private static final String LOCAL_HOST = "127.0.0.1";
    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @Test
    public void testProxyPlain() {
        var httpClient = ohLoader.getClient(ProxyPlainHttpClient.class);
        var callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        var address = (InetSocketAddress) checkNotNull(okHttpClient.proxy()).address();
        assertEquals(LOCAL_HOST, address.getAddress().getHostAddress());
        assertEquals(41111, address.getPort());
    }

    @Test
    public void testProxyProvider() {
        var httpClient = ohLoader.getClient(ProxyProviderHttpClient.class);
        var callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        var address = (InetSocketAddress) checkNotNull(okHttpClient.proxy()).address();
        assertEquals(LOCAL_HOST, address.getAddress().getHostAddress());
        assertEquals(41113, address.getPort());
    }

    @SneakyThrows
    @Test
    public void testProxyParam() {
        var httpClient = ohLoader.getClient(ProxyParamHttpClient.class);
        var proxyParam = new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41115));
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
        var httpClient = ohLoader.getClient(MethodProxyHttpClient.class);
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
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorProxyHttpClient1.class));

        var httpClient = ohLoader.getClient(ErrorProxyHttpClient2.class);
        assertThrows(ProviderException.class, httpClient::sample);
    }

    @OhClient
    @Mapping("${root}:41110")
    @ClientProxy(host = "127.0.0.1", port = 41111)
    public interface ProxyPlainHttpClient {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41112")
    @ClientProxy(proxyProvider = TestProxyProvider.class)
    public interface ProxyProviderHttpClient {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41114")
    @ClientProxy(host = "127.0.0.1", port = 41115)
    public interface ProxyParamHttpClient {

        String sample(Proxy proxy);
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ClientProxyDisabled
    public @interface Disabled {}

    @OhClient
    @Mapping("${root}:41116")
    @ClientProxy(proxyProvider = MethodProxyProvider.class)
    public interface MethodProxyHttpClient {

        String sampleDefault();

        @ClientProxy(host = "127.0.0.1", port = 41118)
        String samplePlain();

        @ClientProxy(proxyProvider = MethodProxyProvider.class)
        String sampleProvider();

        @Disabled
        String sampleDisabled();
    }

    @OhClient
    @Mapping("${root}:41119")
    @ClientProxy(proxyProvider = ErrorProxyProvider.class)
    public interface ErrorProxyHttpClient1 {}

    @OhClient
    @Mapping("${root}:41119")
    @ClientProxy(proxyProvider = NoErrorProxyProvider.class)
    public interface ErrorProxyHttpClient2 {

        @ClientProxy(proxyProvider = ErrorProxyProvider.class)
        String sample();
    }

    public static class TestProxyProvider implements ProxyProvider {

        @Override
        public Proxy proxy(Class<?> clazz) {
            return new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41113));
        }
    }

    public static class MethodProxyProvider implements ProxyProvider {

        @Override
        public Proxy proxy(Class<?> clazz) {
            return new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41117));
        }

        @Override
        public Proxy proxy(Class<?> clazz, Method method) {
            return new Proxy(Type.HTTP, new InetSocketAddress(LOCAL_HOST, 41118));
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
