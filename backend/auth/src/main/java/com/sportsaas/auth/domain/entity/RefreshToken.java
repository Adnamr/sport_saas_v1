package com.sportsaas.auth.domain.entity;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * RefreshToken entity for JWT refresh token management.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RefreshToken extends TenantAwareEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Checks if the token is expired.
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the token is valid (not revoked and not expired).
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * Revokes the token.
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}
