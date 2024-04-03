package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Lab {

    @ApiModelProperty(value = "实验室检查id")
    private String id;

    @ApiModelProperty(value = "实验室检查结果单位(F.r.3.4)")
    private String resultUnstructuredData;

    @ApiModelProperty(value = "报告id")
    private String reportId;
}
