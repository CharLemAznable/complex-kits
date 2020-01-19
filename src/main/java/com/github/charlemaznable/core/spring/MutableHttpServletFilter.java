package com.github.charlemaznable.core.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public final class MutableHttpServletFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest httpServletRequest,
                                    @Nonnull HttpServletResponse httpServletResponse,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
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
