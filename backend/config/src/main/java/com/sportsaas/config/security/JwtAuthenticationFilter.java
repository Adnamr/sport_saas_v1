package com.sportsaas.config.security;

import com.sportsaas.auth.domain.enums.UserRole;
import com.sportsaas.auth.domain.service.JwtService;
import com.sportsaas.tenant.domain.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication filter that validates tokens and sets security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
                UUID userId = jwtService.extractUserId(jwt);
                UUID tenantId = jwtService.extractTenantId(jwt);
                String email = jwtService.extractEmail(jwt);

                // Set tenant context
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                }

                // Create UserPrincipal from token claims
                UserPrincipal userPrincipal = new UserPrincipal(
                    userId,
                    tenantId,
                    email,
                    extractRoleFromToken(jwt)
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private UserRole extractRoleFromToken(String token) {
        try {
            // The role is stored in the token claims
            // We need to extract it - for now default to CUSTOMER
            return UserRole.CUSTOMER;
        } catch (Exception e) {
            return UserRole.CUSTOMER;
        }
    }
}
