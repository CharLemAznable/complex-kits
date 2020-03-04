package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.ResponseParse.ResponseParser;
import com.github.charlemaznable.core.net.common.StatusError;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

class OhRoot {

    Proxy clientProxy;
    SSLSocketFactory sslSocketFactory;
    X509TrustManager x509TrustManager;
    HostnameVerifier hostnameVerifier;
    ConnectionPool connectionPool;
    long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
    long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
    long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
    long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds
    OkHttpClient okHttpClient;

    Charset acceptCharset;
    ContentFormatter contentFormatter;
    HttpMethod httpMethod;
    List<Pair<String, String>> headers;
    List<Pair<String, String>> pathVars;
    List<Pair<String, Object>> parameters;
    List<Pair<String, Object>> contexts;

    Map<HttpStatus, Class<? extends StatusError>> statusErrorMapping;
    Map<HttpStatus.Series, Class<? extends StatusError>> statusSeriesErrorMapping;

    ResponseParser responseParser;
}
