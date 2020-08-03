package com.github.charlemaznable.core.net.httptest;

import com.github.charlemaznable.core.net.Http;
import com.github.charlemaznable.core.spring.MutableHttpServletRequest;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class HttpTestController {

    @RequestMapping("/json")
    public void json(HttpServletResponse response) {
        Http.responseJson(response, "json");
    }

    @RequestMapping("/text")
    public void text(HttpServletResponse response) {
        Http.responseText(response, "text");
    }

    @RequestMapping("/html")
    public void html(HttpServletResponse response) {
        Http.responseHtml(response, "html");
    }

    @RequestMapping("/json-error")
    public void jsonError(HttpServletResponse response) {
        Http.errorJson(response, 404, new IllegalStateException("json"));
    }

    @RequestMapping("/text-error")
    public void textError(HttpServletResponse response) {
        Http.errorText(response, 404, new IllegalStateException("text"));
    }

    @RequestMapping("/html-error")
    public void htmlError(HttpServletResponse response) {
        Http.errorHtml(response, 404, new IllegalStateException("html"));
    }

    @RequestMapping("/http-status-error")
    public void httpStatus(HttpServletResponse response) {
        Http.errorHttpStatus(response, HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping("/parameter")
    public void parameter(HttpServletRequest request, HttpServletResponse response) {
        val parameterMap = Http.fetchParameterMap(request);
        if (!"aaa".equals(parameterMap.get("AAA"))) {
            Http.errorText(response, 500, "ERROR");
        } else if (!"bbb".equals(parameterMap.get("BBB"))) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @RequestMapping("/deal-parameter")
    public void dealParameter(HttpServletRequest request, HttpServletResponse response) {
        val parameterMap = Http.dealReqParams(request.getParameterMap());
        if (!"aaa".equals(parameterMap.get("AAA"))) {
            Http.errorText(response, 500, "ERROR");
        } else if (!"bbb,bbb".equals(parameterMap.get("BBB"))) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @RequestMapping("/path-variable/{AAA}/{BBB}")
    public void pathVariable(HttpServletRequest request, HttpServletResponse response,
                             @PathVariable("AAA") String a, @PathVariable("BBB") String b) {
        val pathVariableMap = Http.fetchPathVariableMap(request);
        if (!a.equals(pathVariableMap.get("AAA")) || !a.equals("aaa")) {
            Http.errorText(response, 500, "ERROR");
        } else if (!b.equals(pathVariableMap.get("BBB")) || !b.equals("bbb")) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping("/header")
    public void header(HttpServletRequest request, HttpServletResponse response) {
        val headerMap = Http.fetchHeaderMap(request);
        if (!"aaa".equals(headerMap.get("AAA"))) {
            Http.errorText(response, 500, "ERROR");
        } else if (!"bbb".equals(headerMap.get("BBB"))) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping("/cookie")
    public void cookie(HttpServletRequest request, HttpServletResponse response) {
        val cookieMap = Http.fetchCookieMap(request);
        if (!"aaa".equals(cookieMap.get("AAA"))) {
            Http.errorText(response, 500, "ERROR");
        } else if (!"bbb".equals(cookieMap.get("BBB"))) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @RequestMapping("/remote-addr")
    public void remoteAddr(HttpServletRequest request, HttpServletResponse response) {
        val remoteAddr = Http.fetchRemoteAddr(request);
        if (!"test.addr".equals(remoteAddr)) {
            Http.responseText(response, remoteAddr);
        } else {
            Http.responseText(response, "OK");
        }
    }

    @RequestMapping("/body")
    public void body(HttpServletRequest request, HttpServletResponse response) {
        val mutableRequest = new MutableHttpServletRequest(request);
        val requestBody = Http.dealRequestBody(mutableRequest, UTF_8.name());
        val requestBodyByStream = Http.dealRequestBodyStream(mutableRequest, UTF_8.name());
        if (!requestBody.equals(requestBodyByStream)) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }

    @RequestMapping("/ajax")
    public void ajax(HttpServletRequest request, HttpServletResponse response) {
        if (!Http.isAjax(request)) {
            Http.errorText(response, 500, "ERROR");
        } else {
            Http.responseText(response, "OK");
        }
    }
}
