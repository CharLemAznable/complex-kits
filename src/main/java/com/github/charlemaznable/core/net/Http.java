package com.github.charlemaznable.core.net;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.github.charlemaznable.core.lang.Str.isNotEmpty;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

public class Http {

    private Http() {}

    public static void responseJson(HttpServletResponse rsp, String json) {
        responseContent(rsp, json, "application/json", UTF_8.name());
    }

    public static void responseText(HttpServletResponse rsp, String text) {
        responseContent(rsp, text, "text/plain", UTF_8.name());
    }

    public static void responseHtml(HttpServletResponse rsp, String html) {
        responseContent(rsp, html, "text/html", UTF_8.name());
    }

    @SneakyThrows
    public static void responseContent(HttpServletResponse rsp, String content,
                                       String contentType, String characterEncoding) {
        if (content == null) return;

        rsp.setHeader("Content-Type", contentType + "; charset=" + characterEncoding);
        rsp.setCharacterEncoding(characterEncoding);
        val writer = rsp.getWriter();
        writer.write(content);
        writer.flush();
    }

    public static Map<String, String> fetchParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = newHashMap();
        val parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            val parameterName = parameterNames.nextElement();
            parameterMap.put(parameterName, request.getParameter(parameterName));
        }
        return parameterMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchPathVariableMap(HttpServletRequest request) {
        Map<String, String> pathVariableMap = newHashMap();
        val pathVariables = request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) pathVariableMap.putAll((Map) pathVariables);
        return pathVariableMap;
    }

    private static final String UNKNOWN = "unknown";

    public static String fetchRemoteAddr(HttpServletRequest request) {
        val xForwardedFor = request.getHeader("x-forwarded-for");
        if (isNotEmpty(xForwardedFor)) {
            val forwardedAddrList = on(",").trimResults().splitToList(xForwardedFor);
            for (val forwardedAddr : forwardedAddrList) {
                if (isNotEmpty(forwardedAddr) &&
                        !UNKNOWN.equalsIgnoreCase(forwardedAddr)) {
                    return forwardedAddr;
                }
            }
        }

        val proxyClientIP = request.getHeader("Proxy-Client-IP");
        if (isNotEmpty(proxyClientIP) &&
                !UNKNOWN.equalsIgnoreCase(proxyClientIP)) {
            return proxyClientIP;
        }

        val wlProxyClientIP = request.getHeader("WL-Proxy-Client-IP");
        if (isNotEmpty(wlProxyClientIP) &&
                !UNKNOWN.equalsIgnoreCase(wlProxyClientIP)) {
            return wlProxyClientIP;
        }

        return request.getRemoteAddr();
    }

    @SneakyThrows
    public static Map<String, String> dealReqParams(Map<String, String[]> requestParams) {
        Map<String, String> params = newHashMap();
        for (val entry : requestParams.entrySet()) {
            val key = entry.getKey();
            val values = entry.getValue();

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
        @Cleanup val dis = new DataInputStream(req.getInputStream());
        val formDataLength = req.getContentLength();
        val buff = new byte[formDataLength];
        var totalBytes = 0;
        while (totalBytes < formDataLength) {
            val bytes = dis.read(buff, totalBytes, formDataLength);
            totalBytes += bytes;
        }
        return new String(buff, charsetName);
    }

    @SneakyThrows
    public static String dealRequestBodyStream(HttpServletRequest req, String charsetName) {
        @Cleanup val isr = new InputStreamReader(req.getInputStream(), charsetName);
        try (val bufferedReader = new BufferedReader(isr)) {
            val stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static void error(HttpServletResponse response, int statusCode, Throwable ex) {
        response.setStatus(statusCode);
        val message = ex.getMessage();
        responseText(response, message != null ? message : ex.toString());
    }

    public static void errorJson(HttpServletResponse response, int statusCode, String json) {
        response.setStatus(statusCode);
        responseJson(response, json);
    }

    public static void errorText(HttpServletResponse response, int statusCode, String text) {
        response.setStatus(statusCode);
        responseText(response, text);
    }

    public static void errorHtml(HttpServletResponse response, int statusCode, String html) {
        response.setStatus(statusCode);
        responseHtml(response, html);
    }
}
