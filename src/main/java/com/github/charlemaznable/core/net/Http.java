package com.github.charlemaznable.core.net;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Str.isNotEmpty;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

public final class Http {

    private static final String UNKNOWN = "unknown";

    private Http() {}

    public static void responseJson(HttpServletResponse response, String json) {
        responseContent(response, json, "application/json", UTF_8.name());
    }

    public static void responseText(HttpServletResponse response, String text) {
        responseContent(response, text, "text/plain", UTF_8.name());
    }

    public static void responseHtml(HttpServletResponse response, String html) {
        responseContent(response, html, "text/html", UTF_8.name());
    }

    @SneakyThrows
    public static void responseContent(HttpServletResponse response, String content,
                                       String contentType, String characterEncoding) {
        if (isNull(content)) return;

        response.setHeader("Content-Type", contentType + "; charset=" + characterEncoding);
        response.setCharacterEncoding(characterEncoding);
        var writer = response.getWriter();
        writer.write(content);
        writer.flush();
    }

    public static void errorJson(HttpServletResponse response, int statusCode, Throwable ex) {
        errorJson(response, statusCode, nullThen(ex.getMessage(), ex::toString));
    }

    public static void errorJson(HttpServletResponse response, int statusCode, String json) {
        response.setStatus(statusCode);
        responseJson(response, json);
    }

    public static void errorText(HttpServletResponse response, int statusCode, Throwable ex) {
        errorText(response, statusCode, nullThen(ex.getMessage(), ex::toString));
    }

    public static void errorText(HttpServletResponse response, int statusCode, String text) {
        response.setStatus(statusCode);
        responseText(response, text);
    }

    public static void errorHtml(HttpServletResponse response, int statusCode, Throwable ex) {
        errorHtml(response, statusCode, nullThen(ex.getMessage(), ex::toString));
    }

    public static void errorHtml(HttpServletResponse response, int statusCode, String html) {
        response.setStatus(statusCode);
        responseHtml(response, html);
    }

    public static void errorHttpStatus(HttpServletResponse response, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        responseText(response, httpStatus.getReasonPhrase());
    }

    public static Map<String, String> fetchParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = newHashMap();
        var parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            var parameterName = parameterNames.nextElement();
            parameterMap.put(parameterName, request.getParameter(parameterName));
        }
        return parameterMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchPathVariableMap(HttpServletRequest request) {
        Map<String, String> pathVariableMap = newHashMap();
        var pathVariables = request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (nonNull(pathVariables)) pathVariableMap.putAll((Map) pathVariables);
        return pathVariableMap;
    }

    public static Map<String, String> fetchHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = newHashMap();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            var headerName = headerNames.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    public static Map<String, String> fetchCookieMap(HttpServletRequest request) {
        Map<String, String> cookieMap = newHashMap();
        var cookies = nullThen(request.getCookies(), () -> new Cookie[]{});
        for (var cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
        }
        return cookieMap;
    }

    public static String fetchRemoteAddr(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("x-forwarded-for");
        if (isNotEmpty(xForwardedFor)) {
            var forwardedAddrList = on(",").trimResults().splitToList(xForwardedFor);
            for (var forwardedAddr : forwardedAddrList) {
                if (isNotEmpty(forwardedAddr) &&
                        !UNKNOWN.equalsIgnoreCase(forwardedAddr)) {
                    return forwardedAddr;
                }
            }
        }

        var proxyClientIP = request.getHeader("Proxy-Client-IP");
        if (isNotEmpty(proxyClientIP) &&
                !UNKNOWN.equalsIgnoreCase(proxyClientIP)) {
            return proxyClientIP;
        }

        var wlProxyClientIP = request.getHeader("WL-Proxy-Client-IP");
        if (isNotEmpty(wlProxyClientIP) &&
                !UNKNOWN.equalsIgnoreCase(wlProxyClientIP)) {
            return wlProxyClientIP;
        }

        return request.getRemoteAddr();
    }

    @SneakyThrows
    public static Map<String, String> dealReqParams(Map<String, String[]> requestParams) {
        Map<String, String> params = newHashMap();
        for (var entry : requestParams.entrySet()) {
            var key = entry.getKey();
            var values = entry.getValue();

            var valueStr = "";
            for (var i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            valueStr = new String(valueStr.getBytes(ISO_8859_1), "gbk");
            params.put(key, valueStr);
        }
        return params;
    }

    @SneakyThrows
    public static String dealRequestBody(HttpServletRequest req, String charsetName) {
        @Cleanup var dis = new DataInputStream(req.getInputStream());
        var formDataLength = req.getContentLength();
        var buff = new byte[formDataLength];
        var totalBytes = 0;
        while (totalBytes < formDataLength) {
            var bytes = dis.read(buff, totalBytes, formDataLength);
            totalBytes += bytes;
        }
        return new String(buff, charsetName);
    }

    @SneakyThrows
    public static String dealRequestBodyStream(HttpServletRequest req, String charsetName) {
        @Cleanup var isr = new InputStreamReader(req.getInputStream(), charsetName);
        try (var bufferedReader = new BufferedReader(isr)) {
            var stringBuilder = new StringBuilder();
            String line;
            while (nonNull(line = bufferedReader.readLine())) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
