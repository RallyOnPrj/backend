package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
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

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected FreeGameSetting() {}
}
