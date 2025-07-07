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
public class SaveLearnedTimeReq {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("EmployeeCoursePlanId")
    private String employeeCoursePlanId;

    @JsonProperty("LearnedTime")
    private Integer learnedTime;

    @JsonProperty("CourseId")
    private String courseId;

    @JsonProperty("CourseResourceId")
    private String courseResourceId;

    @JsonProperty("key")
    private String key;


}
