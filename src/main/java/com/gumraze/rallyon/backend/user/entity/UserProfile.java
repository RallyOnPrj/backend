package com.gumraze.rallyon.backend.user.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends MutableAuditEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private User user;

    private String nickname;
    private String profileImageUrl;

    private LocalDateTime birth;
    private boolean birthVisible;

    // 지역급수, 전국급수
    @Enumerated(EnumType.STRING)
    private Grade regionalGrade;
    @Enumerated(EnumType.STRING)
    private Grade nationalGrade;

    @Column(name = "district_id")
    private UUID districtId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 4, nullable = false)
    private String tag;

    @Column(name = "tag_changed_at", nullable = false)
    private LocalDateTime tagChangedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;    // 계정 생성 시점이 아닌 프로필 생성 시점

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
