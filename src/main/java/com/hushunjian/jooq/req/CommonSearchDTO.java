package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class CommonSearchDTO {

    private List<CommonSearchItemDto> searchItems;

    @ApiModelProperty("无效的报告 true:无效 false:有效 空：全部")
    private Boolean inValid;

    @ApiModelProperty("已删除的报告 1:删除 0:未删除 空：全部")
    private Integer delete;

    @ApiModelProperty("最新版本 true:最新版本 false不是最新版本 空：全部")
    private Boolean newestVersion;

    @ApiModelProperty("项目id过滤")
    private List<String> projectIds;

    @ApiModelProperty("产品id过滤")
    private List<String> productIds;

    @ApiModelProperty(value = "查询语言")
    private String language;
}
