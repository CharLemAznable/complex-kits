package com.github.charlemaznable.spring;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

@UtilityClass
public class MutableHttpServletRequestUtils {

    public MutableHttpServletRequest mutable(HttpServletRequest request) {
        var internalRequest = request;
        while (internalRequest instanceof HttpServletRequestWrapper) {
            if (internalRequest instanceof MutableHttpServletRequest) {
                return (MutableHttpServletRequest) internalRequest;
            }
            internalRequest = internalRequest((HttpServletRequestWrapper) internalRequest);
        }
        return null;
    }

    public void setRequestBody(HttpServletRequest request, String body) {
        val mutableRequest = mutable(request);
        if (null == mutableRequest) return;
        mutableRequest.setRequestBody(body);
    }

    public void setParameter(HttpServletRequest request, String name, Object value) {
        val mutableRequest = mutable(request);
        if (null == mutableRequest) return;
        mutableRequest.setParameter(name, value);
    }

    public void setParameterMap(HttpServletRequest request, Map<String, Object> params) {
        val mutableRequest = mutable(request);
        if (null == mutableRequest) return;
        mutableRequest.setParameterMap(params);
    }

    private HttpServletRequest internalRequest(HttpServletRequestWrapper requestWrapper) {
        return (HttpServletRequest) requestWrapper.getRequest();
    }
}
