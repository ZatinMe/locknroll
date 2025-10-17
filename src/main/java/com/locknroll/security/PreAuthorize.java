package com.locknroll.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom security annotations for role-based access control
 */
public class SecurityAnnotations {

    /**
     * Admin only access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {
    }

    /**
     * Admin or BackOffice access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'BACKOFFICE')")
    public @interface AdminOrBackOffice {
    }

    /**
     * Approver access (Manager, Finance, Quality)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'BACKOFFICE', 'MANAGER', 'FINANCE', 'QUALITY')")
    public @interface ApproverAccess {
    }

    /**
     * Seller access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'BACKOFFICE', 'SELLER')")
    public @interface SellerAccess {
    }

    /**
     * Authenticated user access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public @interface Authenticated {
    }
}
