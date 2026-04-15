package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SonarScanStatusCheckReq {

    @ApiModelProperty(value = "服务id")
    private String appId;

    @ApiModelProperty(value = "分支名称")
    private String branchName;

    @ApiModelProperty(value = "pageNum")
    private Integer pageNum;

    @ApiModelProperty(value = "pageSize")
    private Integer pageSize;
}
