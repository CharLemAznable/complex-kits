package com.github.charlemaznable.spring.mutable;

import com.github.charlemaznable.net.Http;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.github.charlemaznable.codec.Json.json;

@Controller
public class MutableHttpServletFilterController {

    @RequestMapping("/mutable-filter")
    public void mutable(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> requestMap = Http.fetchParameterMap(request);
        requestMap.put("IN_CONTROLLER", "TRUE");
        Http.responseJson(response, json(requestMap));
    }
}
