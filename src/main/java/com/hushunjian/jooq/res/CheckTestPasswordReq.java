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
public class CheckTestPasswordReq {

    @JsonProperty("CourseId")
    private String courseId;

    @JsonProperty("EmployeeCoursePlanId")
    private String employeeCoursePlanId;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("OpenType")
    private String openType;


}
