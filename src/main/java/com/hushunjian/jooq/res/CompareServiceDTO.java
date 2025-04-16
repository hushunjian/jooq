package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareServiceDTO {

    @ApiModelProperty(value = "需要更新的")
    private Map<String, Map<Object, Map<String, Object>>> updatePropertiesMap;

    @ApiModelProperty(value = "需要新增的")
    private Map<String, Map<String, String>> addResultMap;
}
