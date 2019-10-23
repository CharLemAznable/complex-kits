package com.github.charlemaznable.core.net;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpReqTest {

    @SneakyThrows
    @Test
    public void testHttpReqGet() {
        val doGetUrlTemp = "http://test.addr:8080%s";
        val dogetPath = "/doGet";
//        @Cleanup val requestStream = new ByteArrayOutputStream();
        val responseString = "RESPONSE";
        @Cleanup val responseStream = new ByteArrayInputStream(bytes(responseString));

        new MockUp<URL>(URL.class) {
            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpsURLConnection(null) {
                    @Override
                    public String getCipherSuite() {
                        return null;
                    }

                    @Override
                    public Certificate[] getLocalCertificates() {
                        return new Certificate[0];
                    }

                    @Override
                    public Certificate[] getServerCertificates() {
                        return new Certificate[0];
                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

//            @Mock
//            public OutputStream getOutputStream() {
//                return requestStream;
//            }

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.OK.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if ("Content-Type".equals(name)) {
                            return "text/plain; charset=UTF-8";
                        }
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() {
                        return responseStream;
                    }
                };
            }
        };

        val result = new HttpReq(doGetUrlTemp, dogetPath).sslSocketFactory(new SSLSocketFactory() {
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
        }).hostnameVerifier((s, sslSession) -> false).get();
        assertEquals(responseString, result);
    }

    @SneakyThrows
    @Test
    public void testHttpReqGetError() {
        val doGetUrlTemp = "http://test.addr:8080%s";
        val dogetPath = "/doGet";

        new MockUp<URL>(URL.class) {
            @Mock
            public void $init(Invocation invocation, String url) {
                assertEquals(format(doGetUrlTemp, dogetPath), url);
                invocation.proceed(url);
            }

            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpURLConnection(null) {
                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.OK.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        return null;
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        throw new IOException();
                    }
                };
            }
        };
        assertNull(HttpReq.get(doGetUrlTemp, dogetPath));
    }

    @SneakyThrows
    @Test
    public void testHttpReqPost() {
        val doPostRoot = "http://test.addr:8080";
        val doPostPath = "/doPost";
        @Cleanup val requestStream = new ByteArrayOutputStream();
        val responseString = "RESPONSE";
        @Cleanup val responseStream = new ByteArrayInputStream(bytes(responseString));

        new MockUp<URL>(URL.class) {
            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpsURLConnection(null) {
                    @Override
                    public String getCipherSuite() {
                        return null;
                    }

                    @Override
                    public Certificate[] getLocalCertificates() {
                        return new Certificate[0];
                    }

                    @Override
                    public Certificate[] getServerCertificates() {
                        return new Certificate[0];
                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Mock
                    public OutputStream getOutputStream() {
                        return requestStream;
                    }

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.OK.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if ("Content-Type".equals(name)) {
                            return "text/plain; charset=UTF-8";
                        }
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() {
                        return responseStream;
                    }
                };
            }
        };

        var result = new HttpReq(doPostRoot).sslSocketFactory(new SSLSocketFactory() {
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
        }).hostnameVerifier((s, sslSession) -> false).post();
        assertEquals(responseString, result);

        responseStream.reset();

        result = new HttpReq(doPostRoot)
                .req(doPostPath)
                .cookie(null)
                .cookie("TestCookie")
                .params(of("AAA", "aaa", "BBB", "bbb", "Z", "", "", "z"))
                .requestBody("CCC=ccc")
                .post();
        assertEquals(responseString, result);
        assertEquals("AAA=aaa&BBB=bbb&CCC=ccc", requestStream.toString());
    }

    @SneakyThrows
    @Test
    public void testHttpReqPostError() {
        val doPostRoot = "http://test.addr:8080";
        val doPostPath = "/doPost";
        @Cleanup val requestStream = new ByteArrayOutputStream();
        val responseString = "RESPONSE";
        @Cleanup val responseStream = new ByteArrayInputStream(bytes(responseString));

        new MockUp<URL>(URL.class) {
            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpURLConnection(null) {
                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Mock
                    public OutputStream getOutputStream() {
                        return requestStream;
                    }

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.INTERNAL_SERVER_ERROR.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if ("Content-Type".equals(name)) {
                            return "text/plain; charset=UTF-8";
                        }
                        return "";
                    }

                    @Override
                    public InputStream getErrorStream() {
                        return responseStream;
                    }

                    @Override
                    public Map<String, List<String>> getHeaderFields() {
                        return newHashMap();
                    }
                };
            }
        };

        val result = new HttpReq(doPostRoot)
                .req(doPostPath)
                .cookie(null)
                .cookie("TestCookie")
                .requestBody(null)
                .requestBody("CCC=ccc")
                .params(of("AAA", "aaa", "BBB", "bbb", "Z", "", "", "z"))
                .post();
        assertNull(result);
        assertEquals("CCC=ccc&AAA=aaa&BBB=bbb", requestStream.toString());
    }

    @SneakyThrows
    @Test
    public void testHttpReqPostError2() {
        val doPostRoot = "http://test.addr:8080";
        val doPostPath = "/doPost";
        @Cleanup val requestStream = new ByteArrayOutputStream();

        new MockUp<URL>(URL.class) {
            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpURLConnection(null) {
                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Mock
                    public OutputStream getOutputStream() {
                        return requestStream;
                    }

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.INTERNAL_SERVER_ERROR.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if ("Content-Type".equals(name)) {
                            return "text/plain; charset=UTF-8";
                        }
                        return "";
                    }

                    @Override
                    public InputStream getErrorStream() {
                        return null;
                    }

                    @Override
                    public Map<String, List<String>> getHeaderFields() {
                        return newHashMap();
                    }
                };
            }
        };
        val result = new HttpReq(doPostRoot).post();
        assertNull(result);
    }

    @SneakyThrows
    @Test
    public void testHttpReqPostError3() {
        val doPostRoot = "http://test.addr:8080";
        val doPostPath = "/doPost";
        @Cleanup val requestStream = new ByteArrayOutputStream();

        new MockUp<URL>(URL.class) {
            @SneakyThrows
            @Mock
            public URLConnection openConnection() {
                return new HttpURLConnection(null) {
                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Mock
                    public OutputStream getOutputStream() {
                        return requestStream;
                    }

                    @Override
                    public int getResponseCode() {
                        return HttpStatus.OK.value();
                    }

                    @Override
                    public String getHeaderField(String name) {
                        if ("Content-Type".equals(name)) {
                            return "text/plain; charset=UTF-8";
                        }
                        return "";
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        throw new IOException();
                    }
                };
            }
        };
        val result = new HttpReq(doPostRoot).post();
        assertNull(result);
    }
}
