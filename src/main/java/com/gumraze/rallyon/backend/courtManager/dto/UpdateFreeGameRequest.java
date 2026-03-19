package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFreeGameRequest {
    private String title;
    private MatchRecordMode matchRecordMode;
    private GradeType gradeType;

    @Size(max = 255)
    private String location;
    private List<UUID> managerIds;
}
