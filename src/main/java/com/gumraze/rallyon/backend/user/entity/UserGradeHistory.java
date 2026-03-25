package com.gumraze.rallyon.backend.user.entity;

import com.gumraze.rallyon.backend.user.constants.Grade;
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
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_grade_history")
public class UserGradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "identity_account_id", nullable = false)
    private UUID identityAccountId;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    protected UserGradeHistory() {
    }

    public static UserGradeHistory record(
            UUID identityAccountId,
            Grade grade,
            GradeType gradeType
    ) {
        UserGradeHistory history = new UserGradeHistory();
        history.identityAccountId = identityAccountId;
        history.grade = grade;
        history.gradeType = Objects.requireNonNull(gradeType, "gradeType must not be null");
        history.changedAt = LocalDateTime.now();
        return history;
    }
}
