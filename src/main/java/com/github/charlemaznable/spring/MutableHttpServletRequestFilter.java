package com.github.charlemaznable.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(filterName = "MutableHttpServletRequestFilter", urlPatterns = "/*")
public class MutableHttpServletRequestFilter extends OncePerRequestFilter {

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("MutableHttpServletRequestFilter do Filter...");
        filterChain.doFilter(new MutableHttpServletRequest(httpServletRequest), httpServletResponse);
        log.debug("MutableHttpServletRequestFilter done Filter...");
    }
}
