package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class XianShenReportNo {

    @ApiModelProperty(value = "报告id")
    private String id;

    @ApiModelProperty(value = "报告编号")
    private String reportNo;

    @ApiModelProperty(value = "报告分类(C.1.CN.2)")
    private String classifyOfReport;

    @ApiModelProperty(value = "企业报告类型(C.1.3)")
    private String receivedFromId;

    @ApiModelProperty(value = "报告作废/修正(C.1.11.1)")
    private String invalidFix;

    @ApiModelProperty(value = "报告作废/修正原因(C.1.11.2)")
    private String invalidFixReason;

    @ApiModelProperty(value = "加速报告(C.1.7)")
    private String accelerateReport;

    @ApiModelProperty(value = "企业信息来源(C.1.CN.1)")
    private String sourceInfoId;

    @ApiModelProperty(value = "全球唯一编号(C.1.8.1)")
    private String worldUniqueNum;

    @ApiModelProperty("首次收到报告日期")
    private Date firstReceivedDate;

    @ApiModelProperty("收到报告日期")
    private Date reportReceiveDate;
}
