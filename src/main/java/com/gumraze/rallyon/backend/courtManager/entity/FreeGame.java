package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "free_games")
public class FreeGame extends MutableAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;     // 게임을 생성한 유저(FK)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType = GradeType.NATIONAL;        // 참가자들의 급수 형식

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType = GameType.FREE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus = GameStatus.NOT_STARTED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "match_record_mode", nullable = false)
    private MatchRecordMode matchRecordMode = MatchRecordMode.STATUS_ONLY;

    @Column(name = "share_code", length = 64)
    private String shareCode;

    @Column(name = "location", length = 255)
    private String location;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGame() {}

    public static FreeGame create(
            String title,
            User organizer,
            GradeType gradeType,
            MatchRecordMode matchRecordMode,
            String shareCode,
            String location
    ) {
        FreeGame freeGame = new FreeGame();
        freeGame.title = title;
        freeGame.organizer = organizer;
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
}
