package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 报告校验配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCheckConfigDTO {

    @ApiModelProperty(value = "字段配置")
    private List<FieldChekConfigDTO> fieldChekConfigs;

    @ApiModelProperty(value = "字段字典联动配置,A字段字典值调整,B字段已A字段的字典值作为itemClassId,如:评价里的评价方法和评价结果")
    private Map<String, Map<String, String>> fieldItemLinkCheckConfigMap;

    @ApiModelProperty(value = "必须存在的模块DataPath信息")
    private Map<String, String> requiredModuleDataPathMap;

}
