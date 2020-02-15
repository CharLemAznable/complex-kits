package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.CommonReq;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.internal.OhResponseBody;
import com.github.charlemaznable.core.net.ohclient.internal.ResponseBodyExtractor;
import com.github.charlemaznable.core.net.ohclient.internal.StatusErrorFunction;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class OhReq extends CommonReq<OhReq> {

    private static ExecutorService futureExecutorService = newCachedThreadPool();
    private static ConnectionPool globalConnectionPool = new ConnectionPool();

    private Proxy clientProxy;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private HostnameVerifier hostnameVerifier;
    private ConnectionPool connectionPool;

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
        val httpClientBuilder = new OkHttpClient.Builder().proxy(this.clientProxy);
        notNullThen(this.sslSocketFactory, xx -> checkNull(this.x509TrustManager,
                () -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory),
                yy -> httpClientBuilder.sslSocketFactory(this.sslSocketFactory, this.x509TrustManager)));
        notNullThen(this.hostnameVerifier, httpClientBuilder::hostnameVerifier);
        httpClientBuilder.connectionPool(nullThen(this.connectionPool, () -> globalConnectionPool));
        return httpClientBuilder.build();
    }

    private Request buildGetRequest() {
        val requestUrl = concatRequestUrl();
        val parameterMap = fetchParameterMap();
        val requestBuilder = buildCommon();

        requestBuilder.method(HttpMethod.GET.toString(), null);
        val addQuery = this.contentFormatter.format(parameterMap, newHashMap());
        requestBuilder.url(concatRequestQuery(requestUrl, addQuery));
        return requestBuilder.build();
    }

    private Request buildPostRequest() {
        val requestUrl = concatRequestUrl();
        val parameterMap = fetchParameterMap();
        val requestBuilder = buildCommon();

        val content = nullThen(this.requestBody, () ->
                this.contentFormatter.format(parameterMap, newHashMap()));
        requestBuilder.method(HttpMethod.POST.toString(), RequestBody.create(
                MediaType.parse(this.contentFormatter.contentType()), content));
        requestBuilder.url(requestUrl);
        return requestBuilder.build();
    }

    private Request.Builder buildCommon() {
        val requestBuilder = new Request.Builder();
        val acceptCharsetName = this.acceptCharset.name();
        requestBuilder.header(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = this.contentFormatter.contentType();
        requestBuilder.header(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> requestBuilder.removeHeader(header.getKey()),
                    xx -> requestBuilder.header(header.getKey(), header.getValue()));
        }
        return requestBuilder;
    }

    @SneakyThrows
    private String execute(Request request) {
        val response = buildHttpClient().newCall(request).execute();

        val statusCode = response.code();
        val responseBody = notNullThen(response.body(), OhResponseBody::new);

        val errorMapping = new StatusErrorFunction(statusCode, responseBody);
        notNullThen(this.statusErrorMapping.get(
                HttpStatus.valueOf(statusCode)), errorMapping);
        notNullThen(this.statusSeriesErrorMapping.get(
                HttpStatus.Series.valueOf(statusCode)), errorMapping);

        return notNullThen(responseBody, ResponseBodyExtractor::string);
    }
}
