package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Evaluation {

    @ApiModelProperty(value = "评价细分主键id")
    private String id;

    @ApiModelProperty(value = "报告id")
    private String reportId;
}
