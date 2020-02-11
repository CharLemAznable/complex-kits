package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.common.StatusErrorFunction;
import com.github.charlemaznable.core.net.ohclient.internal.OhResponseBody;
import com.github.charlemaznable.core.net.ohclient.internal.ResponseBodyExtractor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.apache.commons.lang3.StringUtils.removeEnd;

public class OhReq {

    private static ExecutorService futureExecutorService = newCachedThreadPool();
    private static ConnectionPool globalConnectionPool = new ConnectionPool();

    private String baseUrl;

    private Proxy clientProxy;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private HostnameVerifier hostnameVerifier;
    private ConnectionPool connectionPool;

    private String reqPath;

    private Charset acceptCharset = DEFAULT_ACCEPT_CHARSET;
    private ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;

    private List<Pair<String, String>> headers = newArrayList();
    private List<Pair<String, Object>> parameters = newArrayList();
    private String requestBody;

    private Map<HttpStatus, Class<? extends StatusError>> statusErrorMapping = newHashMap();
    private Map<HttpStatus.Series, Class<? extends StatusError>> statusSeriesErrorMapping = Mapp.of(
            HttpStatus.Series.CLIENT_ERROR, StatusError.class,
            HttpStatus.Series.SERVER_ERROR, StatusError.class);

    public OhReq() {}

    public OhReq(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public OhReq req(String reqPath) {
        this.reqPath = reqPath;
        return this;
    }

    public OhReq acceptCharset(Charset acceptCharset) {
        this.acceptCharset = acceptCharset;
        return this;
    }

    public OhReq contentFormat(ContentFormatter contentFormatter) {
        this.contentFormatter = contentFormatter;
        return this;
    }

    public OhReq header(String name, String value) {
        this.headers.add(Pair.of(name, value));
        return this;
    }

    public OhReq headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return this;
    }

    public OhReq parameter(String name, String value) {
        this.parameters.add(Pair.of(name, value));
        return this;
    }

    public OhReq parameters(Map<String, String> parameters) {
        parameters.forEach(this::parameter);
        return this;
    }

    public OhReq requestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public OhReq statusErrorMapping(HttpStatus httpStatus, Class<? extends StatusError> errorClass) {
        this.statusErrorMapping.put(httpStatus, errorClass);
        return this;
    }

    public OhReq statusSeriesErrorMapping(HttpStatus.Series httpStatusSeries, Class<? extends StatusError> errorClass) {
        this.statusSeriesErrorMapping.put(httpStatusSeries, errorClass);
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
        if (isBlank(addQuery)) requestBuilder.url(requestUrl);
        else requestBuilder.url(requestUrl +
                (requestUrl.contains("?") ? "&" : "?") + addQuery);
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

    private String concatRequestUrl() {
        if (isBlank(this.reqPath)) return this.baseUrl;
        if (isBlank(this.baseUrl)) return this.reqPath;
        return removeEnd(this.baseUrl, "/") + prependIfMissing(this.reqPath, "/");
    }

    private Map<String, Object> fetchParameterMap() {
        Map<String, Object> parameterMap = newHashMap();
        for (val parameter : this.parameters) {
            parameterMap.put(parameter.getKey(), parameter.getValue());
        }
        return parameterMap;
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
