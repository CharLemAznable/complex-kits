package com.github.charlemaznable.core.spring.mutable;

import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.net.Http.fetchParameterMap;
import static com.github.charlemaznable.core.net.Http.responseJson;

@Controller
public class MutableHttpServletFilterController {

    @RequestMapping("/mutable-filter")
    public void mutable(HttpServletRequest request, HttpServletResponse response) {
        val requestMap = fetchParameterMap(request);
        requestMap.put("IN_CONTROLLER", "TRUE");
        responseJson(response, json(requestMap));
    }
}
