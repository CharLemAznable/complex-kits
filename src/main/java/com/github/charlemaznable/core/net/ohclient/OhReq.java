package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.CommonReq;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.internal.OhResponseBody;
import com.github.charlemaznable.core.net.ohclient.internal.ResponseBodyExtractor;
import com.github.charlemaznable.core.net.ohclient.internal.StatusErrorFunction;
import lombok.SneakyThrows;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class OhReq extends CommonReq<OhReq> {

    private static ExecutorService futureExecutorService = newCachedThreadPool();
    private static ConnectionPool globalConnectionPool = new ConnectionPool();

    private Proxy clientProxy;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private HostnameVerifier hostnameVerifier;
    private ConnectionPool connectionPool;
    private long callTimeout = DEFAULT_CALL_TIMEOUT; // in milliseconds
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // in milliseconds
    private long readTimeout = DEFAULT_READ_TIMEOUT; // in milliseconds
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT; // in milliseconds
    private final List<Interceptor> interceptors = newArrayList();
    private final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    public OhReq() {
        super();
    }

    public OhReq(String baseUrl) {
        super(baseUrl);
    }

    public OhReq clientProxy(Proxy proxy) {
        this.clientProxy = proxy;
        return this;
    }

    public OhReq sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public OhReq x509TrustManager(X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
        return this;
    }

    public OhReq hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public OhReq connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public OhReq callTimeout(long callTimeout) {
        this.callTimeout = callTimeout;
        return this;
    }

    public OhReq connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public OhReq readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public OhReq writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public OhReq addInterceptor(Interceptor interceptor) {
        if (nonNull(interceptor)) {
            this.interceptors.add(interceptor);
        }
        return this;
    }

    public OhReq addInterceptors(Iterable<Interceptor> interceptors) {
        interceptors.forEach(this::addInterceptor);
        return this;
    }

    public OhReq loggingLevel(Level level) {
        this.loggingInterceptor.setLevel(level);
        return this;
    }

    public String get() {
        return this.execute(buildGetRequest());
    }

    public String post() {
        return this.execute(buildPostRequest());
    }

    public Future<String> getFuture() {
        return futureExecutorService.submit(
                () -> execute(buildGetRequest()));
    }

    public Future<String> postFuture() {
        return futureExecutorService.submit(
                () -> execute(buildPostRequest()));
    }

    @SuppressWarnings("deprecation")
    public OkHttpClient buildHttpClient() {
        var httpClientBuilder = new OkHttpClient.Builder().proxy(this.clientProxy);
        notNullThen(this.sslSocketFactory, xx -> notNullThen(this.x509TrustManager, yy ->
                httpClientBuilder.sslSocketFactory(this.sslSocketFactory, this.x509TrustManager)));
        notNullThen(this.hostnameVerifier, httpClientBuilder::hostnameVerifier);
        httpClientBuilder.connectionPool(nullThen(this.connectionPool, () -> globalConnectionPool));
        httpClientBuilder.callTimeout(this.callTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.readTimeout(this.readTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS);
        this.interceptors.forEach(httpClientBuilder::addInterceptor);
        httpClientBuilder.addInterceptor(this.loggingInterceptor);
        return httpClientBuilder.build();
    }

    private Request buildGetRequest() {
        var requestUrl = concatRequestUrl();
        var parameterMap = fetchParameterMap();
        var requestBuilder = buildCommon();

        requestBuilder.method(HttpMethod.GET.toString(), null);
        var addQuery = this.contentFormatter.format(parameterMap, newHashMap());
        requestBuilder.url(concatRequestQuery(requestUrl, addQuery));
        return requestBuilder.build();
    }

    private Request buildPostRequest() {
        var requestUrl = concatRequestUrl();
        var parameterMap = fetchParameterMap();
        var requestBuilder = buildCommon();

        var content = nullThen(this.requestBody, () ->
                this.contentFormatter.format(parameterMap, newHashMap()));
        requestBuilder.method(HttpMethod.POST.toString(), RequestBody.create(
                MediaType.parse(this.contentFormatter.contentType()), content));
        requestBuilder.url(requestUrl);
        return requestBuilder.build();
    }

    private Request.Builder buildCommon() {
        var requestBuilder = new Request.Builder();
        var acceptCharsetName = this.acceptCharset.name();
        requestBuilder.header(ACCEPT_CHARSET, acceptCharsetName);
        var contentType = this.contentFormatter.contentType();
        requestBuilder.header(CONTENT_TYPE, contentType);
        for (var header : this.headers) {
            checkNull(header.getValue(),
                    () -> requestBuilder.removeHeader(header.getKey()),
                    xx -> requestBuilder.header(header.getKey(), header.getValue()));
        }
        return requestBuilder;
    }

    @SneakyThrows
    private String execute(Request request) {
        var response = buildHttpClient().newCall(request).execute();

        var statusCode = response.code();
        var responseBody = notNullThen(response.body(), OhResponseBody::new);

        var errorMapping = new StatusErrorFunction(statusCode, responseBody);
        notNullThen(this.statusErrorMapping.get(
                HttpStatus.valueOf(statusCode)), errorMapping);
        notNullThen(this.statusSeriesErrorMapping.get(
                HttpStatus.Series.valueOf(statusCode)), errorMapping);

        return notNullThen(responseBody, ResponseBodyExtractor::string);
    }
}
