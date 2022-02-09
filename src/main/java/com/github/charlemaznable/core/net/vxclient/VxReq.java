package com.github.charlemaznable.core.net.vxclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.core.net.common.CommonReq;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.vxclient.internal.VxFallbackFunction;
import com.github.charlemaznable.core.net.vxclient.internal.VxStatusErrorThrower;
import com.google.common.collect.Iterators;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.val;
import okhttp3.MediaType;

import java.nio.charset.Charset;
import java.util.Map;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class VxReq extends CommonReq<VxReq> {

    private Vertx vertx;
    private WebClientOptions webClientOptions = new WebClientOptions();
    private Map<HttpStatus, Class<? extends VxFallbackFunction>>
            statusFallbackMapping = newHashMap();
    private Map<HttpStatus.Series, Class<? extends VxFallbackFunction>>
            statusSeriesFallbackMapping = Mapp.of(
            HttpStatus.Series.CLIENT_ERROR, VxStatusErrorThrower.class,
            HttpStatus.Series.SERVER_ERROR, VxStatusErrorThrower.class);

    public VxReq(Vertx vertx) {
        super();
        this.vertx = vertx;
    }

    public VxReq(Vertx vertx, String baseUrl) {
        super(baseUrl);
        this.vertx = vertx;
    }

    public VxReq proxyOptions(ProxyOptions proxyOptions) {
        webClientOptions.setProxyOptions(proxyOptions);
        return this;
    }

    public VxReq keyCertOptions(KeyCertOptions keyCertOptions) {
        webClientOptions.setKeyCertOptions(keyCertOptions);
        return this;
    }

    public VxReq trustOptions(TrustOptions trustOptions) {
        webClientOptions.setTrustOptions(trustOptions);
        return this;
    }

    public VxReq verifyHost(boolean verifyHost) {
        webClientOptions.setVerifyHost(verifyHost);
        return this;
    }

    public VxReq connectTimeout(int connectTimeout) {
        webClientOptions.setConnectTimeout(connectTimeout);
        return this;
    }

    public VxReq statusFallback(HttpStatus httpStatus,
                                Class<? extends VxFallbackFunction> errorClass) {
        this.statusFallbackMapping.put(httpStatus, errorClass);
        return this;
    }

    public VxReq statusSeriesFallback(HttpStatus.Series httpStatusSeries,
                                      Class<? extends VxFallbackFunction> errorClass) {
        this.statusSeriesFallbackMapping.put(httpStatusSeries, errorClass);
        return this;
    }

    @SafeVarargs
    public final void get(Handler<AsyncResult<String>>... handlers) {
        val webClient = buildWebClient();
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersMap = fetchHeaderMap();

        val query = URL_QUERY_FORMATTER.format(parameterMap, newHashMap());
        webClient.getAbs(concatUrlQuery(requestUrl, query))
                .putHeaders(headersMap).send(handle(handlers));
    }

    @SafeVarargs
    public final void post(Handler<AsyncResult<String>>... handlers) {
        val webClient = buildWebClient();
        val parameterMap = fetchParameterMap();
        val requestUrl = concatRequestUrl(parameterMap);
        val headersMap = fetchHeaderMap();

        val content = nullThen(this.requestBody, () ->
                this.contentFormatter.format(parameterMap, newHashMap()));
        val charset = parseCharset(this.contentFormatter.contentType());
        webClient.postAbs(requestUrl).putHeaders(headersMap)
                .sendBuffer(Buffer.buffer(content, charset), handle(handlers));
    }

    public WebClient buildWebClient() {
        return WebClient.create(vertx, webClientOptions);
    }

    private MultiMap fetchHeaderMap() {
        val headersMap = MultiMap.caseInsensitiveMultiMap();
        val acceptCharsetName = this.acceptCharset.name();
        headersMap.set(ACCEPT_CHARSET, acceptCharsetName);
        val contentType = this.contentFormatter.contentType();
        headersMap.set(CONTENT_TYPE, contentType);
        for (val header : this.headers) {
            checkNull(header.getValue(),
                    () -> headersMap.remove(header.getKey()),
                    xx -> headersMap.set(header.getKey(), header.getValue()));
        }
        return headersMap;
    }

    private String parseCharset(String contentType) {
        return checkNull(MediaType.parse(contentType), UTF_8::name, mediaType ->
                checkNull(mediaType.charset(), UTF_8::name, Charset::name));
    }

    @SafeVarargs
    private final Handler<AsyncResult<HttpResponse<Buffer>>> handle(
            Handler<AsyncResult<String>>... handlers) {
        return arResponse -> {
            val promise = Promise.<String>promise();
            if (arResponse.succeeded()) {
                try {
                    val response = arResponse.result();
                    val statusCode = response.statusCode();
                    val responseBody = response.bodyAsString(acceptCharset.name());

                    val statusFallback = statusFallbackMapping
                            .get(HttpStatus.valueOf(statusCode));
                    val statusSeriesFallback = statusSeriesFallbackMapping
                            .get(HttpStatus.Series.valueOf(statusCode));

                    if (nonNull(statusFallback)) {
                        promise.complete(applyFallback(
                                statusFallback, statusCode, responseBody));

                    } else if (nonNull(statusSeriesFallback)) {
                        promise.complete(applyFallback(
                                statusSeriesFallback, statusCode, responseBody));

                    } else promise.complete(responseBody);
                } catch (Exception e) {
                    promise.fail(e);
                }
            } else {
                promise.fail(new VxException(arResponse.cause()));
            }

            iterateHandlers(promise, handlers);
        };
    }

    private String applyFallback(Class<? extends VxFallbackFunction> fallbackClass,
                                 Integer statusCode, String responseBody) {
        return toStr(reflectFactory().build(fallbackClass).apply(statusCode, responseBody));
    }

    @SafeVarargs
    private final void iterateHandlers(Promise<String> promise,
                                       Handler<AsyncResult<String>>... handlers) {
        val iterator = Iterators.forArray(handlers);
        while (iterator.hasNext()) {
            val nextHandler = iterator.next();
            if (isNull(nextHandler)) continue;
            nextHandler.handle(promise.future());
        }
    }
}
