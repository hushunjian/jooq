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
public class ServiceDeployReq {

    @ApiModelProperty(value = "服务id")
    private String appId;

    @ApiModelProperty(value = "分支名称")
    private String branchName;

    @ApiModelProperty(value = "配置版本")
    private String configurationVersion;

    @ApiModelProperty(value = "发布版本")
    private String deploymentVersion;

    @ApiModelProperty(value = "构建参数")
    private List<String> dockerBuildArgs;

    @ApiModelProperty(value = "jdk版本")
    private String jdkVersion;

    @ApiModelProperty(value = "发布环境")
    private String namespace;

    @ApiModelProperty(value = "是否使用缓存")
    private Integer noCache;

    @ApiModelProperty(value = "发布类型")
    private String triggerType;

    @ApiModelProperty(value = "发布申请单")
    private String deployIssueKey;
}
