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
public class CheckTestPasswordRes {

    @JsonProperty("Data")
    private Boolean data;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Message")
    private String message;


}
