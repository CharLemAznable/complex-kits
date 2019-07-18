package com.github.charlemaznable.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@WebFilter(filterName = "MutableHttpServletFilter", urlPatterns = "/*")
public class MutableHttpServletFilter extends OncePerRequestFilter {

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("MutableHttpServletFilter do Filter...");

        val mutableHttpServletRequest = new MutableHttpServletRequest(httpServletRequest);
        val mutableHttpServletResponse = new MutableHttpServletResponse(httpServletResponse);
        filterChain.doFilter(mutableHttpServletRequest, mutableHttpServletResponse);

        val outputStream = httpServletResponse.getOutputStream();
        outputStream.write(mutableHttpServletResponse.getContent());
        outputStream.flush();

        log.debug("MutableHttpServletFilter done Filter...");
    }
}
