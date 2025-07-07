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
public class GetFileInfoRes {

    @JsonProperty("Data")
    private List<FileInfo> data;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("NewFileName")
        private String newFileName;

        @JsonProperty("LearnedTime")
        private Integer learnedTime;

        @JsonProperty("MinLearningTime")
        private Integer minLearningTime;

    }
}
