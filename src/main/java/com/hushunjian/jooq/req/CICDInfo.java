package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CICDInfo {


    /**
     * 映射 Excel 列名：应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 映射 Excel 列名：应用类型
     */
    @ApiModelProperty("应用类型")
    private String appType;

    /**
     * 映射 Excel 列名：代码仓
     */
    @ApiModelProperty("代码仓")
    private String gitFullName;

    /**
     * 映射 Excel 列名：服务名
     */
    @ApiModelProperty("服务名")
    private String serviceName;

    /**
     * 映射 Excel 列名：默认副本数
     */
    @ApiModelProperty("默认副本数")
    private Integer replicaCount;

    /**
     * 映射 Excel 列名：副本最大CPU（核）
     * JSON 中为 Double 类型 (如 1.00, 0.50)
     */
    @ApiModelProperty("副本最大CPU（核）")
    private Double resourceLimitCpu;

    /**
     * 映射 Excel 列名：副本最大内存（Mi）
     */
    @ApiModelProperty("副本最大内存（Mi）")
    private Integer resourceLimitMemory;
}
