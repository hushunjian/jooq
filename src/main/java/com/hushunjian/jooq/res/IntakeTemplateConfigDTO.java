package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class IntakeTemplateConfigDTO {

    @ApiModelProperty("数据ID,新增不传,修改传行数据ID")
    private String id;

    @ApiModelProperty("模板ID")
    private String templateId;

    @ApiModelProperty("配置类型,目前有WORKFLOW、PROMPT")
    private String configType;

    @ApiModelProperty("配置键")
    private String configKey;

    @ApiModelProperty("配置值")
    private String configValue;

    @ApiModelProperty("配置描述")
    private String configDescription;
}
