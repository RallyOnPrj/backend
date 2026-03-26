package com.gumraze.rallyon.backend.user.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
public class UserProfile extends MutableAuditEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    private String nickname;
    private String profileImageUrl;
    private LocalDateTime birth;
    private boolean birthVisible;

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
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfile() {
    }

    public static UserProfile create(
            UUID accountId,
            String nickname,
            UUID districtId,
            Grade regionalGrade,
            Grade nationalGrade,
            LocalDateTime birth,
            Gender gender,
            String tag,
            LocalDateTime now
    ) {
        UserProfile profile = new UserProfile();
        profile.accountId = accountId;
        profile.nickname = nickname;
        profile.districtId = districtId;
        profile.regionalGrade = regionalGrade;
        profile.nationalGrade = nationalGrade;
        profile.birth = birth;
        profile.gender = gender;
        profile.tag = tag;
        profile.tagChangedAt = now;
        profile.birthVisible = false;
        return profile;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeTag(String tag, LocalDateTime changedAt) {
        this.tag = tag;
        this.tagChangedAt = changedAt;
    }

    public void changeRegionalGrade(Grade regionalGrade) {
        this.regionalGrade = regionalGrade;
    }

    public void changeNationalGrade(Grade nationalGrade) {
        this.nationalGrade = nationalGrade;
    }

    public void changeBirth(LocalDateTime birth) {
        this.birth = birth;
    }

    public void changeBirthVisible(boolean birthVisible) {
        this.birthVisible = birthVisible;
    }

    public void changeDistrictId(UUID districtId) {
        this.districtId = districtId;
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void changeGender(Gender gender) {
        this.gender = gender;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTag() {
        return tag;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public LocalDateTime getBirth() {
        return birth;
    }

    public boolean isBirthVisible() {
        return birthVisible;
    }

    public Grade getRegionalGrade() {
        return regionalGrade;
    }

    public Grade getNationalGrade() {
        return nationalGrade;
    }

    public UUID getDistrictId() {
        return districtId;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDateTime getTagChangedAt() {
        return tagChangedAt;
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
