package com.github.charlemaznable.net;

import lombok.Cleanup;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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

    @SneakyThrows
    public static Map<String, String> dealReqParams(Map<String, String[]> requestParams) {
        Map<String, String> params = new HashMap<>();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);

            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
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
