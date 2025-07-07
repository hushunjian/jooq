package com.hushunjian.jooq.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMyStudyTask {

    @JsonProperty("PageSize")
    private Integer pageSize = 10;

    @JsonProperty("CurrentPageIndex")
    private Integer currentPageIndex = 0;

    @JsonProperty("Parameters")
    private Map<String, Object> parameters;
}
