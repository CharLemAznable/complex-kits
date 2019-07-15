package com.github.charlemaznable.spring;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static com.github.charlemaznable.lang.Mapp.newHashMap;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private Map<String, String[]> params;
    private String body;

    public MutableHttpServletRequest(HttpServletRequest request) {
        this(request, "UTF-8");
    }

    @SneakyThrows
    public MutableHttpServletRequest(HttpServletRequest request, String charsetName) {
        super(request);

        this.params = newHashMap(request.getParameterMap());

        @Cleanup val bufferedReader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), charsetName));
        val stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        this.body = stringBuilder.toString();
    }

    @Override
    public String getParameter(String name) {
        val values = this.params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.params;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.params.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.params.get(name);
    }

    public void setParameter(String name, Object value) {
        if (value != null) {
            if (value instanceof String[]) {
                this.params.put(name, (String[]) value);
            } else if (value instanceof String) {
                this.params.put(name, new String[]{(String) value});
            } else {
                this.params.put(name, new String[]{String.valueOf(value)});
            }
        }
    }

    public void setParameterMap(Map<String, Object> params) {
        for (val param : params.entrySet()) {
            setParameter(param.getKey(), param.getValue());
        }
    }

    @SneakyThrows
    @Override
    public ServletInputStream getInputStream() {
        val byteArrayInputStream = new ByteArrayInputStream(this.body.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getRequestBody() {
        return this.body;
    }

    public void setRequestBody(String requestBody) {
        this.body = requestBody;
    }
}
