package com.gumraze.rallyon.backend.identity.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "identity_local_credentials",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_identity_local_credentials_email", columnNames = "email_normalized")
        }
)
public class IdentityLocalCredential extends MutableAuditEntity {

    @Id
    @Column(name = "identity_account_id")
    private UUID identityAccountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "identity_account_id", nullable = false)
    private IdentityAccount identityAccount;

    @Column(name = "email_normalized", nullable = false, length = 320)
    private String emailNormalized;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected IdentityLocalCredential() {
    }

    public static IdentityLocalCredential issue(
            IdentityAccount identityAccount,
            String emailNormalized,
            String passwordHash
    ) {
        IdentityLocalCredential credential = new IdentityLocalCredential();
        credential.identityAccount = identityAccount;
        credential.emailNormalized = emailNormalized;
        credential.passwordHash = passwordHash;
        return credential;
    }

    public UUID getIdentityAccountId() {
        return identityAccountId;
    }

    public IdentityAccount getIdentityAccount() {
        return identityAccount;
    }

    public String getEmailNormalized() {
        return emailNormalized;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    protected void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    protected void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
