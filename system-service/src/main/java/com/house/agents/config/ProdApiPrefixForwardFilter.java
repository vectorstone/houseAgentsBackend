package com.house.agents.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Allows the locally embedded production frontend to call the same /prod-api/*
 * paths that are normally rewritten by the online nginx layer.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProdApiPrefixForwardFilter extends OncePerRequestFilter {

    private static final String PROD_API_PREFIX = "/prod-api";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());
        if (!path.equals(PROD_API_PREFIX) && !path.startsWith(PROD_API_PREFIX + "/")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            private final String rewrittenPath = path.substring(PROD_API_PREFIX.length());

            @Override
            public String getRequestURI() {
                return contextPath + rewrittenPath;
            }

            @Override
            public String getServletPath() {
                return rewrittenPath;
            }
        };
        filterChain.doFilter(wrappedRequest, response);
    }
}
