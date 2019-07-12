package com.github.charlemaznable.spring;

import com.github.charlemaznable.spring.HttpServletRequestConvenientFilter.HttpServletRequestConvenientWrapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

@UtilityClass
public class HttpServletRequestConvenientUtils {

    public HttpServletRequestConvenientWrapper convenientWrapper(HttpServletRequest request) {
        var internalRequest = request;
        while (internalRequest instanceof HttpServletRequestWrapper) {
            if (internalRequest instanceof HttpServletRequestConvenientWrapper) {
                return (HttpServletRequestConvenientWrapper) internalRequest;
            }
            internalRequest = internalRequest((HttpServletRequestWrapper) internalRequest);
        }
        return null;
    }

    public void setParameter(HttpServletRequest request, String name, Object value) {
        val convenientWrapper = convenientWrapper(request);
        if (null == convenientWrapper) return;
        convenientWrapper.setParameter(name, value);
    }

    public void setParameterMap(HttpServletRequest request, Map<String, Object> params) {
        val convenientWrapper = convenientWrapper(request);
        if (null == convenientWrapper) return;
        convenientWrapper.setParameterMap(params);
    }

    private HttpServletRequest internalRequest(HttpServletRequestWrapper requestWrapper) {
        return (HttpServletRequest) requestWrapper.getRequest();
    }
}
