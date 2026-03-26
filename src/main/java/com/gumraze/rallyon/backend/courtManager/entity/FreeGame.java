package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "free_games")
public class FreeGame extends MutableAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "organizer_account_id", nullable = false)
    private UUID organizerAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_record_mode", nullable = false)
    private MatchRecordMode matchRecordMode;

    @Column(name = "share_code", length = 64)
    private String shareCode;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGame() {
    }

    public static FreeGame create(
            String title,
            UUID organizerAccountId,
            GradeType gradeType,
            MatchRecordMode matchRecordMode,
            String shareCode,
            String location
    ) {
        FreeGame freeGame = new FreeGame();
        freeGame.title = title;
        freeGame.organizerAccountId = organizerAccountId;
        freeGame.gradeType = gradeType;
        freeGame.matchRecordMode = matchRecordMode;
        freeGame.shareCode = shareCode;
        freeGame.location = location;
        freeGame.gameType = GameType.FREE;
        freeGame.gameStatus = GameStatus.NOT_STARTED;
        return freeGame;
    }

    public void update(
            String title,
            MatchRecordMode matchRecordMode,
            GradeType gradeType,
            String location
    ) {
        this.title = title != null ? title : this.title;
        this.matchRecordMode = matchRecordMode != null ? matchRecordMode : this.matchRecordMode;
        this.gradeType = gradeType != null ? gradeType : this.gradeType;
        this.location = location != null ? location : this.location;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public UUID getOrganizerAccountId() {
        return organizerAccountId;
    }

    public GradeType getGradeType() {
        return gradeType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public MatchRecordMode getMatchRecordMode() {
        return matchRecordMode;
    }

    public String getShareCode() {
        return shareCode;
    }

    public String getLocation() {
        return location;
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
