package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
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
        name = "game_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"freegame_id", "account_id"})
)
public class GameParticipant extends MutableAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freegame_id", nullable = false)
    private FreeGame freeGame;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(name = "age_group", nullable = false)
    private Integer ageGroup;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected GameParticipant() {
    }

    public static GameParticipant create(
            FreeGame freeGame,
            UUID accountId,
            String originalName,
            String displayName,
            Gender gender,
            Grade grade,
            Integer ageGroup
    ) {
        GameParticipant participant = new GameParticipant();
        participant.freeGame = freeGame;
        participant.accountId = accountId;
        participant.originalName = originalName;
        participant.displayName = displayName;
        participant.gender = gender;
        participant.grade = grade;
        participant.ageGroup = ageGroup;
        return participant;
    }

    public UUID getId() {
        return id;
    }

    public FreeGame getFreeGame() {
        return freeGame;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Gender getGender() {
        return gender;
    }

    public Grade getGrade() {
        return grade;
    }

    public Integer getAgeGroup() {
        return ageGroup;
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
