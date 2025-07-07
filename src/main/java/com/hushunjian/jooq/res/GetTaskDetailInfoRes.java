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
public class GetTaskDetailInfoRes {

    @JsonProperty("Data")
    private TaskDetailInfoRes data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDetailInfoRes {

        private Integer liveType;

        private Integer isOver;

        @JsonProperty("CompleteStateId")
        private Integer completeStateId;

        @JsonProperty("LearnedTime")
        private Integer learnedTime;
    }

}
