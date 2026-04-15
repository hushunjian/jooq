package com.hushunjian.jooq.req;

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
public class ServiceDeployStatusCheckReq {

    @ApiModelProperty(value = "服务id")
    private String appId;

    @ApiModelProperty(value = "分支名称")
    private String branchName;

    @ApiModelProperty(value = "发布环境")
    private List<String> namespaces;

    @ApiModelProperty(value = "当前页")
    private Integer pageNum;

    @ApiModelProperty(value = "分页大小")
    private Integer pageSize;

}
