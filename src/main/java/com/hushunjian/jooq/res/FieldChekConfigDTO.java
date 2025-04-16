package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字段校验配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChekConfigDTO {

    @ApiModelProperty(value = "字段国际化key")
    private String fieldI18nKey;

    @ApiModelProperty(value = "字段路径")
    private String dataPath;

    @ApiModelProperty(value = "字段校验类型")
    private String fieldCheckType;

    @ApiModelProperty(value = "最大长度")
    private Integer maxLength;

    @ApiModelProperty(value = "是否必填,true:必填;null,false:非必填")
    private Boolean required;

    @ApiModelProperty(value = "说明")
    private String describe;

    @ApiModelProperty(value = "配置的字典class")
    private String itemClassId;

    @ApiModelProperty(value = "字典classId支持的字典项uniqueCodes")
    private List<String> itemUniqueCodes;

    @ApiModelProperty(value = "所在表")
    private String table;

    @ApiModelProperty(value = "所在表列")
    private String column;
}
