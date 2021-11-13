package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class JsonInfo {

    @ApiModelProperty(value = "主键ID")
    private String id;


    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "年龄")
    private Integer age;
}
