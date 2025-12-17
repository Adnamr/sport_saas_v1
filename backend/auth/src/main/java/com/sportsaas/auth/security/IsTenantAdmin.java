package com.sportsaas.auth.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict access to TENANT_ADMIN or higher roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public @interface IsTenantAdmin {
}
