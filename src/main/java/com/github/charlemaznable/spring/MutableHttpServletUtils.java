package com.github.charlemaznable.spring;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.nio.charset.Charset;
import java.util.Map;

@UtilityClass
public class MutableHttpServletUtils {

    public MutableHttpServletRequest mutableRequest(HttpServletRequest request) {
        var internalRequest = request;
        while (internalRequest instanceof HttpServletRequestWrapper) {
            if (internalRequest instanceof MutableHttpServletRequest) {
                return (MutableHttpServletRequest) internalRequest;
            }
            internalRequest = internalRequest((HttpServletRequestWrapper) internalRequest);
        }
        return null;
    }

    public MutableHttpServletResponse mutableResponse(HttpServletResponse response) {
        var internalResponse = response;
        while (internalResponse instanceof HttpServletResponseWrapper) {
            if (internalResponse instanceof MutableHttpServletResponse) {
                return (MutableHttpServletResponse) internalResponse;
            }
            internalResponse = internalResponse((HttpServletResponseWrapper) internalResponse);
        }
        return null;
    }

    public void setRequestBody(HttpServletRequest request, String body) {
        val mutableRequest = mutableRequest(request);
        if (null == mutableRequest) return;
        mutableRequest.setRequestBody(body);
    }

    public void setRequestParameter(HttpServletRequest request, String name, Object value) {
        val mutableRequest = mutableRequest(request);
        if (null == mutableRequest) return;
        mutableRequest.setParameter(name, value);
    }

    public void setRequestParameterMap(HttpServletRequest request, Map<String, Object> params) {
        val mutableRequest = mutableRequest(request);
        if (null == mutableRequest) return;
        mutableRequest.setParameterMap(params);
    }

    public byte[] getResponseContent(HttpServletResponse response) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return null;
        return mutableResponse.getContent();
    }

    public void setResponseContent(HttpServletResponse response, byte[] content) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.setContent(content);
    }

    public void appendResponseContent(HttpServletResponse response, byte[] content) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.appendContent(content);
    }

    public String getResponseContentAsString(HttpServletResponse response) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return null;
        return mutableResponse.getContentAsString();
    }

    public void setResponseContentByString(HttpServletResponse response, String content) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.setContentByString(content);
    }

    public void appendResponseContentByString(HttpServletResponse response, String content) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.appendContentByString(content);
    }

    public String getResponseContentAsString(HttpServletResponse response, Charset charset) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return null;
        return mutableResponse.getContentAsString(charset);
    }

    public void setResponseContentByString(HttpServletResponse response, String content, Charset charset) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.setContentByString(content, charset);
    }

    public void appendResponseContentByString(HttpServletResponse response, String content, Charset charset) {
        val mutableResponse = mutableResponse(response);
        if (null == mutableResponse) return;
        mutableResponse.appendContentByString(content, charset);
    }

    private HttpServletRequest internalRequest(HttpServletRequestWrapper requestWrapper) {
        return (HttpServletRequest) requestWrapper.getRequest();
    }

    private HttpServletResponse internalResponse(HttpServletResponseWrapper responseWrapper) {
        return (HttpServletResponse) responseWrapper.getResponse();
    }
}
