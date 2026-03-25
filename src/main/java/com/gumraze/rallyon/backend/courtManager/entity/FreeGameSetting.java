package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "free_game_settings")
public class FreeGameSetting extends MutableAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freegame_id", nullable = false, unique = true)
    private FreeGame freeGame;

    @Column(name = "court_count", nullable = false)
    private Integer courtCount;

    @Column(name = "round_count", nullable = false)
    private Integer roundCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGameSetting() {}

    public static FreeGameSetting create(FreeGame freeGame, Integer courtCount, Integer roundCount) {
        FreeGameSetting setting = new FreeGameSetting();
        setting.freeGame = freeGame;
        setting.courtCount = courtCount;
        setting.roundCount = roundCount;
        return setting;
    }

    public UUID getId() {
        return id;
    }

    public FreeGame getFreeGame() {
        return freeGame;
    }

    public Integer getCourtCount() {
        return courtCount;
    }

    public Integer getRoundCount() {
        return roundCount;
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
