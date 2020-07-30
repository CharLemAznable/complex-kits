package com.github.charlemaznable.core.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.isNull;

public final class MutableHttpServletUtils {

    private MutableHttpServletUtils() {}

    public static MutableHttpServletRequest mutableRequest(HttpServletRequest request) {
        var internalRequest = request;
        while (internalRequest instanceof HttpServletRequestWrapper) {
            if (internalRequest instanceof MutableHttpServletRequest) {
                return (MutableHttpServletRequest) internalRequest;
            }
            internalRequest = internalRequest((HttpServletRequestWrapper) internalRequest);
        }
        return null;
    }

    public static MutableHttpServletResponse mutableResponse(HttpServletResponse response) {
        var internalResponse = response;
        while (internalResponse instanceof HttpServletResponseWrapper) {
            if (internalResponse instanceof MutableHttpServletResponse) {
                return (MutableHttpServletResponse) internalResponse;
            }
            internalResponse = internalResponse((HttpServletResponseWrapper) internalResponse);
        }
        return null;
    }

    public static void setRequestBody(HttpServletRequest request, String body) {
        var mutableRequest = mutableRequest(request);
        if (isNull(mutableRequest)) return;
        mutableRequest.setRequestBody(body);
    }

    public static void setRequestParameter(HttpServletRequest request, String name, Object value) {
        var mutableRequest = mutableRequest(request);
        if (isNull(mutableRequest)) return;
        mutableRequest.setParameter(name, value);
    }

    public static void setRequestParameterMap(HttpServletRequest request, Map<String, Object> params) {
        var mutableRequest = mutableRequest(request);
        if (isNull(mutableRequest)) return;
        mutableRequest.setParameterMap(params);
    }

    public static byte[] getResponseContent(HttpServletResponse response) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return new byte[0];
        return mutableResponse.getContent();
    }

    public static void setResponseContent(HttpServletResponse response, byte[] content) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.setContent(content);
    }

    public static void appendResponseContent(HttpServletResponse response, byte[] content) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.appendContent(content);
    }

    public static String getResponseContentAsString(HttpServletResponse response) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return null;
        return mutableResponse.getContentAsString();
    }

    public static void setResponseContentByString(HttpServletResponse response, String content) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.setContentByString(content);
    }

    public static void appendResponseContentByString(HttpServletResponse response, String content) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.appendContentByString(content);
    }

    public static String getResponseContentAsString(HttpServletResponse response, Charset charset) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return null;
        return mutableResponse.getContentAsString(charset);
    }

    public static void setResponseContentByString(HttpServletResponse response, String content, Charset charset) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.setContentByString(content, charset);
    }

    public static void appendResponseContentByString(HttpServletResponse response, String content, Charset charset) {
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutableResponse.appendContentByString(content, charset);
    }

    public static void mutateResponse(HttpServletResponse response, Consumer<MutableHttpServletResponse> mutator) {
        if (isNull(mutator)) return;
        var mutableResponse = mutableResponse(response);
        if (isNull(mutableResponse)) return;
        mutator.accept(mutableResponse);
    }

    private static HttpServletRequest internalRequest(HttpServletRequestWrapper requestWrapper) {
        return (HttpServletRequest) requestWrapper.getRequest();
    }

    private static HttpServletResponse internalResponse(HttpServletResponseWrapper responseWrapper) {
        return (HttpServletResponse) responseWrapper.getResponse();
    }
}
