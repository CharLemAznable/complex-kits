package com.github.charlemaznable.core.net.common;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;

@SuppressWarnings("unchecked")
public abstract class CommonReq<T extends CommonReq> {

    protected String baseUrl;

    protected String reqPath;

    protected Charset acceptCharset = DEFAULT_ACCEPT_CHARSET;
    protected ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;

    protected List<Pair<String, String>> headers = newArrayList();
    protected List<Pair<String, Object>> parameters = newArrayList();
    protected String requestBody;

    protected Map<HttpStatus, Class<? extends StatusError>>
            statusErrorMapping = newHashMap();
    protected Map<HttpStatus.Series, Class<? extends StatusError>>
            statusSeriesErrorMapping = Mapp.of(
            HttpStatus.Series.CLIENT_ERROR, StatusError.class,
            HttpStatus.Series.SERVER_ERROR, StatusError.class);

    public CommonReq() {
        this(null);
    }

    public CommonReq(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public T req(String reqPath) {
        this.reqPath = reqPath;
        return (T) this;
    }

    public T acceptCharset(Charset acceptCharset) {
        this.acceptCharset = acceptCharset;
        return (T) this;
    }

    public T contentFormat(ContentFormatter contentFormatter) {
        this.contentFormatter = contentFormatter;
        return (T) this;
    }

    public T header(String name, String value) {
        this.headers.add(Pair.of(name, value));
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return (T) this;
    }

    public T parameter(String name, Object value) {
        this.parameters.add(Pair.of(name, value));
        return (T) this;
    }

    public T parameters(Map<String, Object> parameters) {
        parameters.forEach(this::parameter);
        return (T) this;
    }

    public T requestBody(String requestBody) {
        this.requestBody = requestBody;
        return (T) this;
    }

    public T statusErrorMapping(HttpStatus httpStatus,
                                Class<? extends StatusError> errorClass) {
        this.statusErrorMapping.put(httpStatus, errorClass);
        return (T) this;
    }

    public T statusSeriesErrorMapping(HttpStatus.Series httpStatusSeries,
                                      Class<? extends StatusError> errorClass) {
        this.statusSeriesErrorMapping.put(httpStatusSeries, errorClass);
        return (T) this;
    }

    protected String concatRequestUrl() {
        if (isBlank(this.reqPath)) return this.baseUrl;
        if (isBlank(this.baseUrl)) return this.reqPath;
        return this.baseUrl + this.reqPath;
    }

    protected Map<String, Object> fetchParameterMap() {
        Map<String, Object> parameterMap = newHashMap();
        for (var parameter : this.parameters) {
            parameterMap.put(parameter.getKey(), parameter.getValue());
        }
        return parameterMap;
    }

    protected String concatRequestQuery(String requestUrl, String query) {
        if (isBlank(query)) return requestUrl;
        return requestUrl + (requestUrl.contains("?") ? "&" : "?") + query;
    }
}
