package aiss.videominer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(
        name = "Token",
        indexes = {
                @Index(name = "idx_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_token_expires_at", columnList = "expires_at")
        }
)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "token_hash", unique = true, length = 64)
    private String tokenHash;

    @NotNull
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false, columnDefinition = "boolean default false")
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isActiveAt(Instant instant) {
        return !revoked && expiresAt != null && expiresAt.isAfter(instant);
    }
}
