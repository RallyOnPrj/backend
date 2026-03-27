package com.gumraze.rallyon.backend.identity.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.user.constants.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "identity_oauth_links",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_identity_oauth_links_provider_user", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uq_identity_oauth_links_user_provider", columnNames = {"account_id", "provider"})
        }
)
public class OAuthLink extends MutableAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(length = 320)
    private String email;

    @Column(length = 255)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "age_range", length = 20)
    private String ageRange;

    @Column(length = 20)
    private String birthday;

    @Column(name = "is_email_verified")
    private Boolean emailVerified;

    @Column(name = "is_phone_number_verified")
    private Boolean phoneNumberVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected OAuthLink() {
    }

    public static OAuthLink link(
            Account account,
            AuthProvider provider,
            String providerUserId
    ) {
        OAuthLink link = new OAuthLink();
        link.account = account;
        link.provider = provider;
        link.providerUserId = providerUserId;
        return link;
    }

    public void applySnapshot(OAuthUserInfo userInfo) {
        this.email = userInfo.email();
        this.nickname = userInfo.nickname();
        this.profileImageUrl = userInfo.profileImageUrl();
        this.thumbnailImageUrl = userInfo.thumbnailImageUrl();
        this.gender = userInfo.gender();
        this.ageRange = userInfo.ageRange();
        this.birthday = userInfo.birthday();
        this.emailVerified = userInfo.emailVerified();
        this.phoneNumberVerified = userInfo.phoneNumberVerified();
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getNickname() {
        return nickname;
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
