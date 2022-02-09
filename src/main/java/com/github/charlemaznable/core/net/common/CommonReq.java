package com.github.charlemaznable.core.net.common;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.ExtraUrlQuery.ExtraUrlQueryBuilder;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.Url.concatUrlQuery;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONTENT_FORMATTER;

@SuppressWarnings("unchecked")
public abstract class CommonReq<T extends CommonReq> {

    protected static final ContentFormatter URL_QUERY_FORMATTER = new FormContentFormatter();

    protected String baseUrl;

    protected String reqPath;

    protected Charset acceptCharset = DEFAULT_ACCEPT_CHARSET;
    protected ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;

    protected List<Pair<String, String>> headers = newArrayList();
    protected List<Pair<String, Object>> parameters = newArrayList();
    protected String requestBody;

    protected ExtraUrlQueryBuilder extraUrlQueryBuilder;

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

    public T extraUrlQueryBuilder(ExtraUrlQueryBuilder extraUrlQueryBuilder) {
        this.extraUrlQueryBuilder = extraUrlQueryBuilder;
        return (T) this;
    }

    protected Map<String, Object> fetchParameterMap() {
        Map<String, Object> parameterMap = newHashMap();
        for (val parameter : this.parameters) {
            parameterMap.put(parameter.getKey(), parameter.getValue());
        }
        return parameterMap;
    }

    protected String concatRequestUrl(Map<String, Object> parameterMap) {
        val requestUrl = toStr(this.baseUrl).trim() + toStr(this.reqPath).trim();
        val extraUrlQuery = checkNull(this.extraUrlQueryBuilder, () -> "",
                builder -> builder.build(parameterMap, Mapp.newHashMap()));
        return concatUrlQuery(requestUrl, extraUrlQuery);
    }
}
