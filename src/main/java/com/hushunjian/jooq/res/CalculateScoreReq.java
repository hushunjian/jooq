package com.hushunjian.jooq.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateScoreReq {

    @JsonProperty("EmployeeCoursePlanId")
    private String employeeCoursePlanId;

    @JsonProperty("TestType")
    private String testType;

    @JsonProperty("FileType")
    private String fileType;
}
