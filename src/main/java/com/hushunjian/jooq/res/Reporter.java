package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Reporter {

    @ApiModelProperty(value = "报告id")
    private String id;

    @ApiModelProperty(value = "是否首要报告者")
    private String primaryReporter;

    @ApiModelProperty(value = "所在国家/地区")
    private String reporterCountry;
}
