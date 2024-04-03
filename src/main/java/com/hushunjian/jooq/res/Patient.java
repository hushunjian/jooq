package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Patient {

    @ApiModelProperty(value = "患者主键id")
    private String id;
}
