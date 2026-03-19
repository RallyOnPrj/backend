package com.gumraze.rallyon.backend.user.entity;

import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_grade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    /**
     * Create a record of a user's grade change and timestamp the change.
     *
     * @param user the user whose grade changed
     * @param grade the new grade assigned to the user
     * @param gradeType the nature of the grade change (e.g., how or why the grade was changed)
     */
    public UserGradeHistory(
            User user,
            Grade grade,
            GradeType gradeType
    ) {
        this.user = user;
        this.grade = grade;
        this.gradeType = Objects.requireNonNull(gradeType, "gradeType must not be null");
        this.changedAt = LocalDateTime.now();
    }
}
