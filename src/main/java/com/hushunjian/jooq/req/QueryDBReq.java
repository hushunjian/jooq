package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @ApiModelProperty(value = "统计SQL")
    private String countSql;

    @ApiModelProperty(value = "导出所有")
    private Boolean exportAll;

    @ApiModelProperty(value = "文件下载cookie")
    private String downloadFileCookie;

    @ApiModelProperty(value = "下载错误的SQL信息")
    private List<String> queryErrorSqlInfos;

    @ApiModelProperty(value = "文件信息")
    private Map<String, String> fileInfoMap;

    @ApiModelProperty(value = "esafety5调用的Cookie")
    private String esafety5Cookie;
}
