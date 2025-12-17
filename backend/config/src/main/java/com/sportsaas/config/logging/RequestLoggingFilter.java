package com.sportsaas.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that logs HTTP request and response information.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (shouldLog(uri)) {
                log.info("HTTP {} {} - {} ({}ms)", method, fullPath, status, duration);
            }
        }
    }

    private boolean shouldLog(String uri) {
        // Skip logging for health checks and static resources
        return !uri.startsWith("/actuator")
                && !uri.startsWith("/swagger")
                && !uri.startsWith("/api-docs")
                && !uri.startsWith("/favicon")
                && !uri.endsWith(".js")
                && !uri.endsWith(".css")
                && !uri.endsWith(".html");
    }
}
