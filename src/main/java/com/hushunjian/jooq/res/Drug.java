package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Drug {

    @ApiModelProperty(value = "产品id")
    private String id;

    @ApiModelProperty(value = "商品名称")
    private String brandName;

    @ApiModelProperty(value = "通用名称")
    private String genericName;

    @ApiModelProperty(value = "报告id")
    private String reportId;

    @ApiModelProperty(value = "")
    private String manufacture;

    private String psurDrugId;

    private String drugType;

    private String createTime;

    private Integer unblindingType;

    private String selfDrugProduct;
}
