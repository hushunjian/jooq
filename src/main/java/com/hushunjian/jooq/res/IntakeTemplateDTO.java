package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IntakeTemplateDTO {

    @ApiModelProperty("数据ID,新增不传,修改传行数据ID")
    private String id;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("模板描述")
    private String templateDescription;

    @ApiModelProperty("执行策略")
    private String intakeExecutionStrategyEnum;

    @ApiModelProperty("租户ID")
    private String tenantId;

    @ApiModelProperty("模板配置")
    private List<IntakeTemplateConfigDTO> configs;

    @ApiModelProperty("来源模板ID")
    private String sourceTemplateId;

    @ApiModelProperty("模板文件ID")
    private String templateFileId;

    @ApiModelProperty("模板文件名称")
    private String templateFileName;

}
