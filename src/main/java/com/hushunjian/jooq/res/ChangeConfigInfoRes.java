package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeConfigInfoRes {

    @ApiModelProperty(value = "prod配置")
    private String prodConfig;

    @ApiModelProperty(value = "新配置")
    private String newConfig;
}
