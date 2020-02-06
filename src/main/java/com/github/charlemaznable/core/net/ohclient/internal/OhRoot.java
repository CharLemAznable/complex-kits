package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.ContentFormat;
import com.github.charlemaznable.core.net.ohclient.exception.OhError;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

class OhRoot {

    Proxy proxy;
    SSLSocketFactory sslSocketFactory;
    X509TrustManager x509TrustManager;
    HostnameVerifier hostnameVerifier;
    ConnectionPool connectionPool;
    OkHttpClient okHttpClient;

    Charset acceptCharset;
    ContentFormat contentFormat;
    RequestMethod requestMethod;
    List<Pair<String, String>> headers;
    List<Pair<String, String>> pathVars;
    List<Pair<String, Object>> parameters;
    List<Pair<String, Object>> contexts;

    Map<HttpStatus, Class<? extends OhError>> statusMapping;
    Map<HttpStatus.Series, Class<? extends OhError>> statusSeriesMapping;
}
