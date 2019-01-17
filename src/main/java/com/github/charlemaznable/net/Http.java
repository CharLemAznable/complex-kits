package com.github.charlemaznable.net;

import lombok.Cleanup;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.lang.Str.isNotEmpty;
import static com.google.common.base.Charsets.ISO_8859_1;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Maps.newHashMap;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

public class Http {

    public static void responseJson(HttpServletResponse rsp, String json) {
        responseContent(rsp, json, "application/json", "UTF-8");
    }

    public static void responseText(HttpServletResponse rsp, String text) {
        responseContent(rsp, text, "text/plain", "UTF-8");
    }

    public static void responseHtml(HttpServletResponse rsp, String html) {
        responseContent(rsp, html, "text/html", "UTF-8");
    }

    @SneakyThrows
    public static void responseContent(HttpServletResponse rsp, String content,
                                       String contentType, String characterEncoding) {
        if (content == null) return;

        rsp.setHeader("Content-Type", contentType + "; charset=" + characterEncoding);
        rsp.setCharacterEncoding(characterEncoding);
        PrintWriter writer = rsp.getWriter();
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public static Map<String, String> fetchParameterMap(HttpServletRequest request) {
        Map<String, String> parameterMap = newHashMap();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            parameterMap.put(parameterName, request.getParameter(parameterName));
        }
        return parameterMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchPathVariableMap(HttpServletRequest request) {
        Map<String, String> pathVariableMap = newHashMap();
        Object pathVariables = request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) pathVariableMap.putAll((Map) pathVariables);
        return pathVariableMap;
    }

    public static String fetchRemoteAddr(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("x-forwarded-for");
        if (isNotEmpty(xForwardedFor)) {
            List<String> forwardedAddrList = on(",").trimResults().splitToList(xForwardedFor);
            for (String forwardedAddr : forwardedAddrList) {
                if (isNotEmpty(forwardedAddr) &&
                        !"unknown".equalsIgnoreCase(forwardedAddr)) {
                    return forwardedAddr;
                }
            }
        }

        String proxyClientIP = request.getHeader("Proxy-Client-IP");
        if (isNotEmpty(proxyClientIP) &&
                !"unknown".equalsIgnoreCase(proxyClientIP)) {
            return proxyClientIP;
        }

        String wlProxyClientIP = request.getHeader("WL-Proxy-Client-IP");
        if (isNotEmpty(wlProxyClientIP) &&
                !"unknown".equalsIgnoreCase(wlProxyClientIP)) {
            return wlProxyClientIP;
        }

        return request.getRemoteAddr();
    }

    @SneakyThrows
    public static Map<String, String> dealReqParams(Map<String, String[]> requestParams) {
        Map<String, String> params = newHashMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);

            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            valueStr = new String(valueStr.getBytes(ISO_8859_1), "gbk");
            params.put(key, valueStr);
        }
        return params;
    }

    @SneakyThrows
    public static String dealRequestBody(HttpServletRequest req, String charsetName) {
        @Cleanup DataInputStream dis = new DataInputStream(req.getInputStream());
        int formDataLength = req.getContentLength();
        byte buff[] = new byte[formDataLength];
        int totalBytes = 0;
        while (totalBytes < formDataLength) {
            int bytes = dis.read(buff, totalBytes, formDataLength);
            totalBytes += bytes;
        }
        return new String(buff, charsetName);
    }

    @SneakyThrows
    public static String dealRequestBodyStream(HttpServletRequest req, String charsetName) {
        @Cleanup InputStreamReader isr = new InputStreamReader(req.getInputStream(), charsetName);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static void error(HttpServletResponse response, int statusCode, Throwable ex) {
        response.setStatus(statusCode);
        String message = ex.getMessage();
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
