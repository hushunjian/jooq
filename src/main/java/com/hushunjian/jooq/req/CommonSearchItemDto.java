package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CommonSearchItemDto {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("路径节点id")
    private String pageId;
    @ApiModelProperty("字段记录id")
    private String fieldId;
    @ApiModelProperty("多个数据以OR分割，时间范围以TO分隔")
    private String searchValue;
    @ApiModelProperty(value = "1:精确,2:模糊,3:时间类型介于,4:字符串数字左包含(左模糊),5:字符串数字右包含(右模糊)")
    private Integer searchMode;
}
