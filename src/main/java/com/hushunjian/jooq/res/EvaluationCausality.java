package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EvaluationCausality {

    @ApiModelProperty(value = "评价细分主键id")
    private String id;

    @ApiModelProperty(value = "评价id")
    private String causalityId;

    @ApiModelProperty(value = "评价方法")
    private String evaluationMethod;

    @ApiModelProperty(value = "评价方法细分")
    private String evaluationMethodBreakdown;

    @ApiModelProperty(value = "评价来源")
    private String evaluationSource;

    @ApiModelProperty(value = "评价结果")
    private String evaluationResult;
}
