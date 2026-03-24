package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(
        name = "game_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"freegame_id", "user_id"})
)
public class GameParticipant extends MutableAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freegame_id", nullable = false)
    private FreeGame freeGame;

    // 비회원 참가 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "original_name", nullable = false)
    private String originalName;

    @NotNull
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

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected GameParticipant() {}
}
