package com.github.charlemaznable.core.net.vxclient;

import com.github.charlemaznable.core.net.common.CommonReq;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.vxclient.internal.StatusErrorFunction;
import com.google.common.collect.Iterators;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.val;
import okhttp3.MediaType;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

public class VxReq extends CommonReq<VxReq> {

    private Vertx vertx;
    private WebClientOptions webClientOptions = new WebClientOptions();

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

    @SafeVarargs
    public final void get(Handler<AsyncResult<String>>... handlers) {
        val webClient = buildWebClient();
        val requestUrl = concatRequestUrl();
        val parameterMap = fetchParameterMap();
        val headersMap = fetchHeaderMap();

        val addQuery = this.contentFormatter.format(parameterMap, newHashMap());
        webClient.getAbs(concatRequestQuery(requestUrl, addQuery))
                .putHeaders(headersMap).send(handle(handlers));
    }

    @SafeVarargs
    public final void post(Handler<AsyncResult<String>>... handlers) {
        val webClient = buildWebClient();
        val requestUrl = concatRequestUrl();
        val parameterMap = fetchParameterMap();
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
        val headersMap = new VertxHttpHeaders();
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

                    val errorMapping = new StatusErrorFunction(statusCode, responseBody);
                    notNullThen(statusErrorMapping.get(
                            HttpStatus.valueOf(statusCode)), errorMapping);
                    notNullThen(statusSeriesErrorMapping.get(
                            HttpStatus.Series.valueOf(statusCode)), errorMapping);

                    promise.complete(responseBody);
                } catch (Exception e) {
                    promise.fail(e);
                }
            } else {
                promise.fail(new VxException(arResponse.cause()));
            }

            val iterator = Iterators.forArray(handlers);
            while (iterator.hasNext()) {
                val nextHandler = iterator.next();
                if (isNull(nextHandler)) continue;
                nextHandler.handle(promise.future());
            }
        };
    }
}
