package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.HostnameVerifierProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.SSLSocketFactoryProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSL.X509TrustManagerProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigSSLDisabled;
import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.Test;
import sun.security.ssl.SSLContextImpl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnusedReturnValue")
public class SSLProviderTest {

    @Test
    public void testSSLDef() {
        val httpClient = getClient(SSLDefHttpClient.class);
        val callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        assertTrue(okHttpClient.sslSocketFactory() instanceof TestSSLSocketFactory);
        assertTrue(okHttpClient.hostnameVerifier() instanceof TestHostnameVerifier);
    }

    @Test
    public void testSSLAll() {
        val httpClient = getClient(SSLAllHttpClient.class);
        val callback = on(httpClient).field("CGLIB$CALLBACK_0").get();
        OkHttpClient okHttpClient = on(callback).field("okHttpClient").get();
        assertTrue(okHttpClient.sslSocketFactory() instanceof TestSSLSocketFactory);
        assertTrue(okHttpClient.hostnameVerifier() instanceof TestHostnameVerifier);
    }

    @SneakyThrows
    @Test
    public void testSSLDefParam() {
        val httpClient = getClient(SSLDefParamHttpClient.class);
        val sslSocketFactory = new TestSSLSocketFactory();
        val hostnameVerifier = new TestHostnameVerifier();
        try {
            httpClient.sample(sslSocketFactory, hostnameVerifier);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41122", e.getMessage());
        }
        try {
            httpClient.sample(null, null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41122", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testSSLAllParam() {
        val httpClient = getClient(SSLAllParamHttpClient.class);
        val sslSocketFactory = new TestSSLSocketFactory();
        val x509TrustManager = new TestX509TrustManager();
        val hostnameVerifier = new TestHostnameVerifier();
        try {
            httpClient.sample(sslSocketFactory, x509TrustManager, hostnameVerifier);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41123", e.getMessage());
        }
        try {
            httpClient.sample(null, null, null);
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41123", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testMethodSSL() {
        val httpClient = getClient(MethodSSLHttpClient.class);
        try {
            httpClient.sample();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41124", e.getMessage());
        }
        try {
            httpClient.sampleDef();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41124", e.getMessage());
        }
        try {
            httpClient.sampleAll();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41124", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testDisabledSSL() {
        val httpClient = getClient(DisableSSLHttpClient.class);
        try {
            httpClient.sample();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41124", e.getMessage());
        }
        try {
            httpClient.sampleDisabled();
        } catch (Exception e) {
            assertEquals("Failed to connect to /127.0.0.1:41124", e.getMessage());
        }
    }

    @SneakyThrows
    @Test
    public void testErrorSSL() {
        assertThrows(OhException.class, () ->
                getClient(ErrorSSLHttpClient1.class));
        assertThrows(OhException.class, () ->
                getClient(ErrorSSLHttpClient2.class));
        assertThrows(OhException.class, () ->
                getClient(ErrorSSLHttpClient3.class));

        val httpClient = getClient(ErrorSSLHttpClient4.class);
        assertThrows(OhException.class, httpClient::error1);
        assertThrows(OhException.class, httpClient::error2);
        assertThrows(OhException.class, httpClient::error3);
    }

    @OhClient("${root}:41120")
    @OhConfigSSL(
            sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
            hostnameVerifierProvider = TestHostnameVerifierProvider.class)
    public interface SSLDefHttpClient {

        String sample();
    }

    @OhClient("${root}:41121")
    @OhConfigSSL(
            sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
            x509TrustManagerProvider = TestX509TrustManagerProvider.class,
            hostnameVerifierProvider = TestHostnameVerifierProvider.class)
    public interface SSLAllHttpClient {

        String sample();
    }

    @OhClient("${root}:41122")
    @OhConfigSSL(
            sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
            hostnameVerifierProvider = TestHostnameVerifierProvider.class)
    public interface SSLDefParamHttpClient {

        String sample(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier);
    }

    @OhClient("${root}:41123")
    @OhConfigSSL(
            sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
            x509TrustManagerProvider = TestX509TrustManagerProvider.class,
            hostnameVerifierProvider = TestHostnameVerifierProvider.class)
    public interface SSLAllParamHttpClient {

        String sample(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager, HostnameVerifier hostnameVerifier);
    }

    @OhClient("${root}:41124")
    public interface MethodSSLHttpClient {

        String sample();

        @OhConfigSSL(
                sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
                hostnameVerifierProvider = TestHostnameVerifierProvider.class)
        String sampleDef();

        @OhConfigSSL(
                sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
                x509TrustManagerProvider = TestX509TrustManagerProvider.class,
                hostnameVerifierProvider = TestHostnameVerifierProvider.class)
        String sampleAll();
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @OhConfigSSLDisabled
    public @interface Disabled {}

    @OhClient("${root}:41124")
    @OhConfigSSL(
            sslSocketFactoryProvider = TestSSLSocketFactoryProvider.class,
            hostnameVerifierProvider = TestHostnameVerifierProvider.class)
    public interface DisableSSLHttpClient {

        String sample();

        @Disabled
        String sampleDisabled();
    }

    @OhClient("${root}:41125")
    @OhConfigSSL(
            sslSocketFactoryProvider = ErrorSSLSocketFactoryProvider.class)
    public interface ErrorSSLHttpClient1 {}

    @OhClient("${root}:41125")
    @OhConfigSSL(
            sslSocketFactoryProvider = NoErrorSSLSocketFactoryProvider.class,
            x509TrustManagerProvider = ErrorX509TrustManagerProvider.class)
    public interface ErrorSSLHttpClient2 {}

    @OhClient("${root}:41125")
    @OhConfigSSL(
            sslSocketFactoryProvider = NoErrorSSLSocketFactoryProvider.class,
            x509TrustManagerProvider = NoErrorX509TrustManagerProvider.class,
            hostnameVerifierProvider = ErrorHostnameVerifierProvider.class)
    public interface ErrorSSLHttpClient3 {}

    @OhClient("${root}:41125")
    @OhConfigSSL(
            sslSocketFactoryProvider = NoErrorSSLSocketFactoryProvider.class,
            x509TrustManagerProvider = NoErrorX509TrustManagerProvider.class,
            hostnameVerifierProvider = NoErrorHostnameVerifierProvider.class)
    public interface ErrorSSLHttpClient4 {

        @OhConfigSSL(
                sslSocketFactoryProvider = ErrorSSLSocketFactoryProvider.class)
        String error1();

        @OhConfigSSL(
                sslSocketFactoryProvider = NoErrorSSLSocketFactoryProvider.class,
                x509TrustManagerProvider = ErrorX509TrustManagerProvider.class)
        String error2();

        @OhConfigSSL(
                sslSocketFactoryProvider = NoErrorSSLSocketFactoryProvider.class,
                x509TrustManagerProvider = NoErrorX509TrustManagerProvider.class,
                hostnameVerifierProvider = ErrorHostnameVerifierProvider.class)
        String error3();
    }

    public static class TestSSLSocketFactory extends SSLSocketFactory {

        private SSLContextImpl context;

        {
            try {
                context = new SSLContextImpl.DefaultSSLContext();
            } catch (Exception e) {
                // ignore
            }
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return new String[0];
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[0];
        }

        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) {
            return null;
        }

        @Override
        public Socket createSocket(String s, int i) {
            return null;
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) {
            return null;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) {
            return null;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) {
            return null;
        }
    }

    public static class TestSSLSocketFactoryProvider implements SSLSocketFactoryProvider {

        @Override
        public SSLSocketFactory sslSocketFactory(Class<?> clazz) {
            return new TestSSLSocketFactory();
        }

        @Override
        public SSLSocketFactory sslSocketFactory(Class<?> clazz, Method method) {
            return new TestSSLSocketFactory();
        }
    }

    public static class TestX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TestX509TrustManagerProvider implements X509TrustManagerProvider {

        @Override
        public X509TrustManager x509TrustManager(Class<?> clazz) {
            return new TestX509TrustManager();
        }

        @Override
        public X509TrustManager x509TrustManager(Class<?> clazz, Method method) {
            return new TestX509TrustManager();
        }
    }

    public static class TestHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    public static class TestHostnameVerifierProvider implements HostnameVerifierProvider {

        @Override
        public HostnameVerifier hostnameVerifier(Class<?> clazz) {
            return new TestHostnameVerifier();
        }

        @Override
        public HostnameVerifier hostnameVerifier(Class<?> clazz, Method method) {
            return new TestHostnameVerifier();
        }
    }

    public static class ErrorSSLSocketFactoryProvider implements SSLSocketFactoryProvider {}

    public static class NoErrorSSLSocketFactoryProvider implements SSLSocketFactoryProvider {

        @Override
        public SSLSocketFactory sslSocketFactory(Class<?> clazz) {
            return new TestSSLSocketFactory();
        }

        @Override
        public SSLSocketFactory sslSocketFactory(Class<?> clazz, Method method) {
            return new TestSSLSocketFactory();
        }
    }

    public static class ErrorX509TrustManagerProvider implements X509TrustManagerProvider {}

    public static class NoErrorX509TrustManagerProvider implements X509TrustManagerProvider {

        @Override
        public X509TrustManager x509TrustManager(Class<?> clazz) {
            return new TestX509TrustManager();
        }

        @Override
        public X509TrustManager x509TrustManager(Class<?> clazz, Method method) {
            return new TestX509TrustManager();
        }
    }

    public static class ErrorHostnameVerifierProvider implements HostnameVerifierProvider {}

    public static class NoErrorHostnameVerifierProvider implements HostnameVerifierProvider {

        @Override
        public HostnameVerifier hostnameVerifier(Class<?> clazz) {
            return new TestHostnameVerifier();
        }

        @Override
        public HostnameVerifier hostnameVerifier(Class<?> clazz, Method method) {
            return new TestHostnameVerifier();
        }
    }
}
