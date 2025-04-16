package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixData {


    @ApiModelProperty(value = "表")
    private String table;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "列字段值")
    private Map<String, String> columnValueMap;
}
