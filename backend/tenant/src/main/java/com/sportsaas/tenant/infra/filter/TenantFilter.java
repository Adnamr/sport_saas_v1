package com.sportsaas.tenant.infra.filter;

import com.sportsaas.tenant.domain.TenantContext;
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
import java.util.UUID;

/**
 * Filter that extracts the tenant ID from the X-Tenant-ID header
 * and sets it in the TenantContext for the duration of the request.
 *
 * <p>The filter runs early in the filter chain to ensure the tenant context
 * is available for all subsequent processing.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    /**
     * Paths that don't require a tenant ID.
     */
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/",
        "/api/public/",
        "/api/admin/tenants",
        "/actuator/",
        "/swagger-ui",
        "/v3/api-docs",
        "/health",
        "/info"
    };

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String tenantIdHeader = request.getHeader(TENANT_HEADER);

            if (tenantIdHeader != null && !tenantIdHeader.isBlank()) {
                try {
                    UUID tenantId = UUID.fromString(tenantIdHeader.trim());
                    TenantContext.setTenantId(tenantId);
                    log.debug("Tenant context set: {}", tenantId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid tenant ID format in header: {}", tenantIdHeader);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
                    return;
                }
            } else if (requiresTenant(request)) {
                log.warn("Missing X-Tenant-ID header for path: {}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-Tenant-ID header is required");
                return;
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Determines if the request path requires a tenant ID.
     *
     * @param request the HTTP request
     * @return true if tenant ID is required
     */
    private boolean requiresTenant(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return false;
            }
        }

        // API endpoints require tenant
        return path.startsWith("/api/");
    }
}
