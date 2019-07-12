package com.github.charlemaznable.spring;

import com.google.common.collect.Iterators;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Map;

import static com.github.charlemaznable.lang.Mapp.newHashMap;

@Slf4j
@Component
@WebFilter(filterName = "HttpServletRequestConvenientFilter", urlPatterns = "/*")
public class HttpServletRequestConvenientFilter extends OncePerRequestFilter {

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("HttpServletRequestConvenientFilter do Filter...");
        filterChain.doFilter(new HttpServletRequestConvenientWrapper(httpServletRequest, "UTF-8"), httpServletResponse);
        log.debug("HttpServletRequestConvenientFilter done Filter...");
    }

    public static class HttpServletRequestConvenientWrapper extends HttpServletRequestWrapper {

        private Map<String, String[]> params;
        @Getter
        private final String body;

        @SneakyThrows
        public HttpServletRequestConvenientWrapper(HttpServletRequest request, String charsetName) {
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

        @SneakyThrows
        @Override
        public ServletInputStream getInputStream() {
            val byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
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

        @Override
        public String getParameter(String name) {
            val values = params.get(name);
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
            return Iterators.asEnumeration(this.params.keySet().iterator());
        }

        @Override
        public String[] getParameterValues(String name) {
            return this.params.get(name);
        }

        public void setParameter(String name, Object value) {
            if (value != null) {
                if (value instanceof String[]) {
                    params.put(name, (String[]) value);
                } else if (value instanceof String) {
                    params.put(name, new String[]{(String) value});
                } else {
                    params.put(name, new String[]{String.valueOf(value)});
                }
            }
        }

        public void setParameterMap(Map<String, Object> params) {
            for (val param : params.entrySet()) {
                setParameter(param.getKey(), param.getValue());
            }
        }
    }
}
