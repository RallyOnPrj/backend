package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFreeGameRequest {
    private String title;
    private MatchRecordMode matchRecordMode;
    private GradeType gradeType;
    private List<Long> managerIds;
}
