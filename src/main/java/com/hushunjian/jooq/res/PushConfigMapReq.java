package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushConfigMapReq {

    @ApiModelProperty(value = "服务id")
    private String appId;

    @ApiModelProperty(value = "分支名称")
    private String branchName;

    @ApiModelProperty(value = "配置版本")
    private String configurationVersion;

    @ApiModelProperty(value = "空间")
    private String namespace;
}
