package com.hushunjian.jooq.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshReportEsReq {

    @ApiModelProperty(value = "租户")
    private List<String> tenantIds;

    @ApiModelProperty(value = "开始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @ApiModelProperty(value = "刷新当前日期的")
    private Boolean refreshCurrentDate;

    @ApiModelProperty(value = "页大小")
    private Integer pageSize;

    @ApiModelProperty(value = "休眠时间")
    private Integer sleep;

    @ApiModelProperty(value = "报告ids")
    private List<String> reportIds;

    @JsonIgnore
    @ApiModelProperty(value = "租户主语言")
    private String locale;

    //    private String defaultStartDate = DateFormatUtils.format(DateUtils.addMonths(new Date(), -1),"yyyy-MM-dd");
    private Date currentDate = new Date();
}
