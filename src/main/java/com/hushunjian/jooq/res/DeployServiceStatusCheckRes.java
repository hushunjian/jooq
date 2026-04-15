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
public class DeployServiceStatusCheckRes {

    @ApiModelProperty(value = "发布状态详情")
    private List<DeployServiceStatusCheckDetailRes> list;
}
