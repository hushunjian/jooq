package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployServiceStatusCheckDetailRes {

    private String appId;

    private String branchName;

    private String namespace;

    private String pipelineStatus;
}
