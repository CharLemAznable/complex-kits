package com.github.charlemaznable.spring;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Component
@WebFilter(filterName = "HttpServletRequestBodyReaderFilter", urlPatterns = "/*")
public class HttpServletRequestBodyReaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        log.debug("HttpServletRequestBodyReaderFilter init...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("HttpServletRequestBodyReaderFilter doFilter...");
        chain.doFilter(request instanceof HttpServletRequest ?
                new HttpServletRequestBodyReaderWrapper((HttpServletRequest) request, "UTF-8") : request, response);
    }

    @Override
    public void destroy() {
        log.debug("HttpServletRequestBodyReaderFilter destroy...");
    }

    static class HttpServletRequestBodyReaderWrapper extends HttpServletRequestWrapper {

        @Getter
        private final String body;

        @SneakyThrows
        public HttpServletRequestBodyReaderWrapper(HttpServletRequest request, String charsetName) {
            super(request);

            @Cleanup val bufferedReader = new BufferedReader(
                    new InputStreamReader(request.getInputStream(), charsetName));
            val stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            body = stringBuilder.toString();
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
    }
}
