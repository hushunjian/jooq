package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryDBReq {
    @ApiModelProperty(value = "cookie")
    private String cookie;

    @ApiModelProperty(value = "csrfToken")
    private String csrfToken;

    @ApiModelProperty(value = "实例名")
    private String instanceName;

    @ApiModelProperty(value = "数据库名")
    private String dbName;

    @ApiModelProperty(value = "查询数据库返回条数")
    private String limitNum;

    @ApiModelProperty(value = "SQL语句")
    private String sqlContent;

    @ApiModelProperty(value = "文件名称")
    private String fileName;
}
