package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmQueryDetailReq {

    private String query;

    private ApmQueryDetailConditionReq variables;

    private String cookie;
}
