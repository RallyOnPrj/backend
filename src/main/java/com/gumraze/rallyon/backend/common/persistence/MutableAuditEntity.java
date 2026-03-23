package com.gumraze.rallyon.backend.common.persistence;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class MutableAuditEntity {

    public abstract LocalDateTime getCreatedAt();

    protected abstract void setCreatedAt(LocalDateTime createdAt);

    public abstract LocalDateTime getUpdatedAt();

    protected abstract void setUpdatedAt(LocalDateTime updatedAt);

    @PrePersist
    protected void prePersistAudit() {
        LocalDateTime now = LocalDateTime.now();
        if (getCreatedAt() == null) {
            setCreatedAt(now);
        }
        if (getUpdatedAt() == null) {
            setUpdatedAt(now);
        }
    }

    @PreUpdate
    protected void preUpdateAudit() {
        setUpdatedAt(LocalDateTime.now());
    }
}
