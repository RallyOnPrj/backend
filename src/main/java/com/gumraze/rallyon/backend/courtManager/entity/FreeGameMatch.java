package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(
        name = "free_game_match",
        uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "court_number"})
)
@NoArgsConstructor
public class FreeGameMatch extends MutableAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private FreeGameRound round;

    @Column(name = "court_number", nullable = false)
    private Integer courtNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player1_id")
    private GameParticipant teamAPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_player2_id")
    private GameParticipant teamAPlayer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player1_id")
    private GameParticipant teamBPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_player2_id")
    private GameParticipant teamBPlayer2;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    private MatchResult matchResult;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;       // 코트 삭제 시 삭제된 코트를 표시하기 위함, 소프트 삭제용

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
