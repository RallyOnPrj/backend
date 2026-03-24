package com.gumraze.rallyon.backend.common.persistence;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class CreatedAtEntity {

    public abstract LocalDateTime getCreatedAt();

    protected abstract void setCreatedAt(LocalDateTime createdAt);

    @PrePersist
    protected void prePersistCreatedAt() {
        if (getCreatedAt() == null) {
            setCreatedAt(LocalDateTime.now());
        }
    }
}
