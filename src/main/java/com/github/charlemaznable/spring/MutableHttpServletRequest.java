package com.github.charlemaznable.spring;

import lombok.AllArgsConstructor;
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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.lang.Mapp.newHashMap;
import static com.google.common.base.Charsets.UTF_8;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private Map<String, String[]> params;
    private String content;
    private Charset charset;

    public MutableHttpServletRequest(HttpServletRequest request) {
        this(request, UTF_8);
    }

    @SneakyThrows
    public MutableHttpServletRequest(HttpServletRequest request, Charset charset) {
        super(request);

        this.params = newHashMap(request.getParameterMap());

        @Cleanup val bufferedReader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), charset));
        val stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        this.content = stringBuilder.toString();
        this.charset = charset;
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

    @Override
    public ServletInputStream getInputStream() {
        return new MutableServletInputStream(
                new ByteArrayInputStream(bytes(this.content, this.charset)));
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getRequestBody() {
        return this.content;
    }

    public void setRequestBody(String requestBody) {
        this.content = requestBody;
    }

    @AllArgsConstructor
    static class MutableServletInputStream extends ServletInputStream {

        private ByteArrayInputStream byteArrayInputStream;

        public int read() {
            return this.byteArrayInputStream.read();
        }

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
    }
}
