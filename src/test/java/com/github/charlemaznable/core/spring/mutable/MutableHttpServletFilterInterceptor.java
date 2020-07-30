package com.github.charlemaznable.core.spring.mutable;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.getResponseContentAsString;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setRequestParameter;
import static com.github.charlemaznable.core.spring.MutableHttpServletUtils.setResponseContentByString;

@Component
public class MutableHttpServletFilterInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        setRequestParameter(request, "IN_PREHANDLE", "TRUE");
        return true;
    }

    @Override
    public void postHandle(@Nonnull HttpServletRequest request,
                           @Nonnull HttpServletResponse response,
                           @Nonnull Object handler, ModelAndView modelAndView) {
        var responseContentAsString = getResponseContentAsString(response);
        var responseMap = unJson(responseContentAsString);
        responseMap.put("IN_POSTHANDLE", "TRUE");
        setResponseContentByString(response, json(responseMap));
    }
}
