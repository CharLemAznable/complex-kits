package com.github.charlemaznable.spring.mutable;

import com.github.charlemaznable.spring.MutableHttpServletUtils;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static com.github.charlemaznable.codec.Json.json;
import static com.github.charlemaznable.codec.Json.unJson;

@Component
public class MutableHttpServletFilterInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MutableHttpServletUtils.setRequestParameter(request, "IN_PREHANDLE", "TRUE");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        val responseContentAsString = MutableHttpServletUtils.getResponseContentAsString(response);
        Map<String, Object> responseMap = unJson(responseContentAsString);
        responseMap.put("IN_POSTHANDLE", "TRUE");
        MutableHttpServletUtils.setResponseContentByString(response, json(responseMap));
    }
}
