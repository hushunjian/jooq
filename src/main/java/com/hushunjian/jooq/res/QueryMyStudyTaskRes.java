package com.hushunjian.jooq.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMyStudyTaskRes {

    @JsonProperty("Data")
    private List<MyStudyTaskDetail> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyStudyTaskDetail{

        @JsonProperty("Id")
        private String id;

        @JsonProperty("Code")
        private String code;

        @JsonProperty("EmployeeCoursePlanId")
        private String employeeCoursePlanId;

        @JsonProperty("CompleteStateId")
        private Integer completeStateId;

        @JsonProperty("OverdueState")
        private Integer overdueState;
    }
}
