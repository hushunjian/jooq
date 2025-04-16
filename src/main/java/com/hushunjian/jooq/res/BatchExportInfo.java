package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BatchExportInfo {

    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "报告id")
    private String reportId;

    @ApiModelProperty(value = "报告编号")
    private String reportNo;


    @ApiModelProperty(value = "文件类型")
    private String fileTypeUniqueCode;


    // e2b用不到,方便前端处理,传入后在传出
    @ApiModelProperty(value = "监管机构递交方式")
    private String submitMethodId;

    @ApiModelProperty(value = "标识符信息")
    private IdentificationRes identification;

    @ApiModelProperty(value = "监管机构")
    private String regulatory;
}
