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
public class CopyEnvConfigReq {

    @ApiModelProperty(value = "服务id")
    private String appId;

    @ApiModelProperty(value = "source")
    private String sourceNamespace;

    @ApiModelProperty(value = "targets")
    private List<String> targetNamespaces;
}
